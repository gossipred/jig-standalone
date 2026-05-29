package com.jj.jig.jig;

import com.jj.jig.log.JigLog;
import com.jj.jig.log.JigLogActionType;
import com.jj.jig.log.JigLogRepository;
import com.jj.jig.user.User;
import com.jj.jig.user.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JigService {

    private static final Pattern JIG_NO_PREFIX_PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z]{2}$");
    private static final Pattern JIG_BASE_NO_PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z]{2}-([0-9]{3})$");

    private final JigRepository jigRepository;
    private final JigFileRepository jigFileRepository;
    private final JigFileStorageService jigFileStorageService;
    private final JigLogRepository jigLogRepository;
    private final UserRepository userRepository;

    public JigService(
            JigRepository jigRepository,
            JigFileRepository jigFileRepository,
            JigFileStorageService jigFileStorageService,
            JigLogRepository jigLogRepository,
            UserRepository userRepository
    ) {
        this.jigRepository = jigRepository;
        this.jigFileRepository = jigFileRepository;
        this.jigFileStorageService = jigFileStorageService;
        this.jigLogRepository = jigLogRepository;
        this.userRepository = userRepository;
    }

    public List<Jig> findJigs(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return jigRepository.findAll();
        }

        String trimmedKeyword = keyword.trim();
        List<Jig> results = new ArrayList<>(jigRepository.searchByListKeyword(trimmedKeyword));
        addStatusMatches(results, trimmedKeyword);
        return results;
    }

    public Jig findById(Long id) {
        return jigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jig not found: " + id));
    }

    public String nextJigNo(String prefix) {
        String normalizedPrefix = normalizeJigNoPrefix(prefix);
        OptionalInt maxSequence = jigRepository.findByJigBaseNoStartingWith(normalizedPrefix + "-").stream()
                .map(Jig::getJigBaseNo)
                .map(JigService::extractBaseSequence)
                .filter(OptionalInt::isPresent)
                .mapToInt(OptionalInt::getAsInt)
                .max();

        int nextSequence = maxSequence.orElse(0) + 1;
        if (nextSequence > 999) {
            throw new IllegalArgumentException("Jig No. sequence has reached 999 for " + normalizedPrefix + ".");
        }
        return normalizedPrefix + "-" + String.format("%03d", nextSequence);
    }

    @Transactional
    public Jig create(JigForm form, String username) {
        Jig jig = new Jig();
        applyForm(jig, form);
        User user = findUser(username);
        jig.setCreatedBy(user);
        jig.setUpdatedBy(user);
        jig.setDri(blankToNull(username));

        if (jigRepository.existsByJigNo(jig.getJigNo())) {
            throw new IllegalArgumentException("Jig No. already exists.");
        }

        Jig savedJig = jigRepository.save(jig);
        storeUploadedFiles(savedJig, form);
        saveSimpleLog(savedJig, user, JigLogActionType.CREATE, "Created jig " + savedJig.getJigNo());
        return savedJig;
    }

    @Transactional
    public Jig update(Long id, JigForm form, String username) {
        Jig jig = findById(id);
        JigStatus oldStatus = jig.getStatus();
        LocalDate oldDueDate = jig.getDueDate();
        applyForm(jig, form);
        User user = findUser(username);
        jig.setUpdatedBy(user);
        jig.setDri(blankToNull(username));

        if (jigRepository.existsByJigNoAndIdNot(jig.getJigNo(), id)) {
            throw new IllegalArgumentException("Jig No. already exists.");
        }

        Jig savedJig = jigRepository.save(jig);
        storeUploadedFiles(savedJig, form);
        boolean statusChanged = oldStatus != savedJig.getStatus();
        boolean dueDateChanged = !Objects.equals(oldDueDate, savedJig.getDueDate());
        if (statusChanged) {
            saveStatusChangeLog(savedJig, user, oldStatus, savedJig.getStatus(), "Status changed from Edit Jig form.");
        }
        if (dueDateChanged) {
            saveDueDateChangeLog(savedJig, user, oldDueDate, savedJig.getDueDate(), "Due date changed from Edit Jig form.");
        }
        if (!statusChanged && !dueDateChanged) {
            saveSimpleLog(savedJig, user, JigLogActionType.UPDATE, "Updated jig master data.");
        }
        return savedJig;
    }

    public List<JigFile> findFiles(Long jigId) {
        return jigFileRepository.findByJigIdOrderByUploadedAtDesc(jigId);
    }

    public List<JigLog> findLogs(Long jigId) {
        return jigLogRepository.findByJigIdOrderByCreatedAtDesc(jigId);
    }

    public JigFile findFileById(Long fileId) {
        return jigFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
    }

    public Resource loadFileResource(JigFile jigFile) {
        return jigFileStorageService.loadAsResource(jigFile.getStoredPath());
    }

    @Transactional
    public Long deleteFile(Long fileId, String username) {
        JigFile jigFile = findFileById(fileId);
        Jig jig = jigFile.getJig();
        Long jigId = jigFile.getJig().getId();
        String storedPath = jigFile.getStoredPath();
        String originalFilename = jigFile.getOriginalFilename();
        jigFileRepository.delete(jigFile);
        jigFileStorageService.deleteIfExists(storedPath);
        saveSimpleLog(jig, findUser(username), JigLogActionType.FILE_DELETE, "Deleted file: " + originalFilename);
        return jigId;
    }

    @Transactional
    public Long replaceFile(Long fileId, MultipartFile replacementFile, String username) {
        JigFile jigFile = findFileById(fileId);
        if (replacementFile == null || replacementFile.isEmpty()) {
            throw new IllegalArgumentException("Please choose a replacement file.");
        }

        long oldSize = jigFile.getFileSize() != null ? jigFile.getFileSize() : 0L;
        long existingTotal = jigFileRepository.sumFileSizeByJigId(jigFile.getJig().getId());
        long sizeAfterReplace = existingTotal - oldSize + replacementFile.getSize();
        if (sizeAfterReplace > MAX_TOTAL_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Replacing this file would exceed the 100MB total limit for this jig. "
                    + "Current total (excluding this file): " + formatFileSize(existingTotal - oldSize)
                    + ", new file: " + formatFileSize(replacementFile.getSize()) + ".");
        }

        String oldStoredPath = jigFile.getStoredPath();
        String oldFilename = jigFile.getOriginalFilename();
        JigFileStorageService.StoredJigFile storedFile = jigFileStorageService.store(replacementFile, jigFile.getJig().getJigNo());
        jigFile.setOriginalFilename(storedFile.originalFilename());
        jigFile.setStoredPath(storedFile.storedPath());
        jigFile.setContentType(storedFile.contentType());
        jigFile.setFileSize(storedFile.fileSize());
        jigFile.setUploadedAt(LocalDateTime.now());
        jigFileRepository.save(jigFile);
        jigFileStorageService.deleteIfExists(oldStoredPath);
        saveSimpleLog(
                jigFile.getJig(),
                findUser(username),
                JigLogActionType.FILE_REPLACE,
                "Replaced file: " + oldFilename + " -> " + storedFile.originalFilename()
        );
        return jigFile.getJig().getId();
    }

    @Transactional
    public void uploadFiles(Long jigId, List<Path> filePaths, String username) {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }
        Jig jig = findById(jigId);

        long existingCount = jigFileRepository.countByJigId(jigId);
        if (existingCount + filePaths.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException(
                    "A jig can have at most " + MAX_FILE_COUNT + " files "
                    + "(currently " + existingCount + ", trying to add " + filePaths.size() + ").");
        }

        long newFilesSize = 0;
        for (Path p : filePaths) {
            try { newFilesSize += Files.size(p); } catch (IOException ignored) { }
        }
        long existingTotalSize = jigFileRepository.sumFileSizeByJigId(jigId);
        if (existingTotalSize + newFilesSize > MAX_TOTAL_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Total file size would exceed the 100MB limit for this jig "
                    + "(current: " + formatFileSize(existingTotalSize)
                    + ", adding: " + formatFileSize(newFilesSize) + ").");
        }

        int uploaded = 0;
        for (Path filePath : filePaths) {
            String originalFilename = filePath.getFileName().toString();
            String contentType = null;
            try { contentType = Files.probeContentType(filePath); } catch (IOException ignored) { }

            JigFileStorageService.StoredJigFile storedFile =
                    jigFileStorageService.store(filePath, originalFilename, contentType, jig.getJigNo());
            if (storedFile == null) {
                continue;
            }
            JigFile jigFile = new JigFile();
            jigFile.setJig(jig);
            jigFile.setOriginalFilename(storedFile.originalFilename());
            jigFile.setStoredPath(storedFile.storedPath());
            jigFile.setContentType(storedFile.contentType());
            jigFile.setFileSize(storedFile.fileSize());
            jigFileRepository.save(jigFile);
            uploaded++;
        }

        if (uploaded > 0) {
            saveSimpleLog(jig, findUser(username), JigLogActionType.FILE_UPLOAD,
                    "Uploaded " + uploaded + " file(s).");
        }
    }

    @Transactional
    public void deleteJig(Long id, String username) {
        Jig jig = findById(id);

        List<JigFile> files = jigFileRepository.findByJigIdOrderByUploadedAtDesc(id);
        for (JigFile f : files) {
            jigFileStorageService.deleteIfExists(f.getStoredPath());
        }
        jigFileRepository.deleteAll(files);

        jigLogRepository.deleteAll(jigLogRepository.findByJigIdOrderByCreatedAtDesc(id));

        jigRepository.delete(jig);
    }

    @Transactional
    public Jig updateStatus(Long id, JigStatus newStatus, String note, String username) {
        Jig jig = findById(id);
        JigStatus oldStatus = jig.getStatus();
        boolean statusChanged = (oldStatus != newStatus);
        String trimmedNote = blankToNull(note);

        if (!statusChanged && trimmedNote == null) {
            return jig;
        }

        User user = findUser(username);

        if (statusChanged) {
            jig.setStatus(newStatus);
            jig.setUpdatedBy(user);
        }
        Jig savedJig = statusChanged ? jigRepository.save(jig) : jig;

        JigLog log = new JigLog();
        log.setJig(savedJig);
        log.setUser(user);
        if (statusChanged) {
            log.setActionType(JigLogActionType.STATUS_CHANGE);
            log.setOldStatus(oldStatus);
            log.setNewStatus(newStatus);
        } else {
            log.setActionType(JigLogActionType.NOTE);
        }
        log.setNote(trimmedNote);
        jigLogRepository.save(log);
        return savedJig;
    }

    @Transactional
    public Jig updateDueDate(Long id, LocalDate newDueDate, String note, String username) {
        Jig jig = findById(id);
        LocalDate oldDueDate = jig.getDueDate();
        if ((oldDueDate == null && newDueDate == null) || (oldDueDate != null && oldDueDate.equals(newDueDate))) {
            return jig;
        }

        User user = findUser(username);
        jig.setDueDate(newDueDate);
        jig.setDueDateUpdatedAt(LocalDateTime.now());
        jig.setDueDateUpdatedBy(user);
        jig.setUpdatedBy(user);
        Jig savedJig = jigRepository.save(jig);

        JigLog log = new JigLog();
        log.setJig(savedJig);
        log.setUser(user);
        log.setActionType(JigLogActionType.DUE_DATE_CHANGE);
        log.setOldDueDate(oldDueDate);
        log.setNewDueDate(newDueDate);
        log.setNote(blankToNull(note));
        jigLogRepository.save(log);
        return savedJig;
    }

    private void applyForm(Jig jig, JigForm form) {
        JigNumber jigNumber = JigNumber.parse(form.getJigNo());
        jig.setClassification(blankToNull(form.getClassification()));
        jig.setModelName(form.getModelName().trim());
        jig.setJigName(form.getJigName().trim());
        jig.setCustomer(blankToNull(form.getCustomer()));
        jig.setJigNo(jigNumber.jigNo());
        jig.setJigBaseNo(jigNumber.jigBaseNo());
        jig.setSetNo(jigNumber.setNo());
        jig.setAssemblyLine(blankToNull(form.getAssemblyLine()));
        jig.setJigPictureUrl(blankToNull(form.getJigPictureUrl()));
        jig.setQuantity(form.getQuantity());
        jig.setMroNo(blankToNull(form.getMroNo()));
        jig.setPrNo(blankToNull(form.getPrNo()));
        jig.setStatus(form.getStatus());
        jig.setDri(blankToNull(form.getDri()));
        jig.setNote(blankToNull(form.getNote()));
        jig.setStartDate(form.getStartDate());
        jig.setDueDate(form.getDueDate());
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeJigNoPrefix(String prefix) {
        String normalizedPrefix = StringUtils.hasText(prefix) ? prefix.trim().toUpperCase(Locale.ROOT) : "AM-ME";
        if (!JIG_NO_PREFIX_PATTERN.matcher(normalizedPrefix).matches()) {
            throw new IllegalArgumentException("Jig No. prefix must be like AM-ME.");
        }
        return normalizedPrefix;
    }

    private static OptionalInt extractBaseSequence(String jigBaseNo) {
        if (!StringUtils.hasText(jigBaseNo)) {
            return OptionalInt.empty();
        }
        Matcher matcher = JIG_BASE_NO_PATTERN.matcher(jigBaseNo.trim().toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(matcher.group(1)));
    }

    private void addStatusMatches(List<Jig> results, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        jigRepository.findAll().stream()
                .filter(jig -> jig.getStatus() != null)
                .filter(jig -> statusMatchesKeyword(jig.getStatus(), normalizedKeyword))
                .filter(jig -> results.stream().noneMatch(result -> result.getId().equals(jig.getId())))
                .forEach(results::add);
    }

    private boolean statusMatchesKeyword(JigStatus status, String normalizedKeyword) {
        return status.name().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                || status.getLabel().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                || status.getLabelZh().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private User findUser(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean isOwner(Long jigId, String username) {
        Jig jig = findById(jigId);
        return jig.getCreatedBy() != null && jig.getCreatedBy().getUsername().equals(username);
    }

    private void saveSimpleLog(Jig jig, User user, JigLogActionType actionType, String note) {
        JigLog log = new JigLog();
        log.setJig(jig);
        log.setUser(user);
        log.setActionType(actionType);
        log.setNote(note);
        jigLogRepository.save(log);
    }

    private void saveStatusChangeLog(Jig jig, User user, JigStatus oldStatus, JigStatus newStatus, String note) {
        JigLog log = new JigLog();
        log.setJig(jig);
        log.setUser(user);
        log.setActionType(JigLogActionType.STATUS_CHANGE);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setNote(note);
        jigLogRepository.save(log);
    }

    private void saveDueDateChangeLog(Jig jig, User user, LocalDate oldDueDate, LocalDate newDueDate, String note) {
        JigLog log = new JigLog();
        log.setJig(jig);
        log.setUser(user);
        log.setActionType(JigLogActionType.DUE_DATE_CHANGE);
        log.setOldDueDate(oldDueDate);
        log.setNewDueDate(newDueDate);
        log.setNote(note);
        jigLogRepository.save(log);
    }

    private static final int MAX_FILE_COUNT = 11;
    private static final long MAX_TOTAL_FILE_SIZE_BYTES = 100L * 1024 * 1024; // 100 MB

    private void storeUploadedFiles(Jig jig, JigForm form) {
        List<MultipartFile> files = nonEmptyFiles(form.getJigFiles());
        if (files.isEmpty()) {
            return;
        }

        long existingFileCount = jigFileRepository.countByJigId(jig.getId());
        if (existingFileCount + files.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException(
                    "A jig can have at most " + MAX_FILE_COUNT + " files "
                    + "(currently " + existingFileCount + ", trying to add " + files.size() + ").");
        }

        long existingTotalSize = jigFileRepository.sumFileSizeByJigId(jig.getId());
        long newFilesSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (existingTotalSize + newFilesSize > MAX_TOTAL_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Total file size would exceed the 100MB limit for this jig "
                    + "(current: " + formatFileSize(existingTotalSize)
                    + ", adding: " + formatFileSize(newFilesSize) + ").");
        }

        String firstStoredPath = null;
        for (MultipartFile file : files) {
            JigFileStorageService.StoredJigFile storedFile = jigFileStorageService.store(file, jig.getJigNo());
            if (storedFile == null) {
                continue;
            }

            JigFile jigFile = new JigFile();
            jigFile.setJig(jig);
            jigFile.setOriginalFilename(storedFile.originalFilename());
            jigFile.setStoredPath(storedFile.storedPath());
            jigFile.setContentType(storedFile.contentType());
            jigFile.setFileSize(storedFile.fileSize());
            jigFileRepository.save(jigFile);

            if (firstStoredPath == null) {
                firstStoredPath = storedFile.storedPath();
            }
        }

        if (firstStoredPath != null && !StringUtils.hasText(jig.getJigPictureUrl())) {
            jig.setJigPictureUrl(firstStoredPath);
            jigRepository.save(jig);
        }
    }

    // ===== Phase 4 — Log search & Stats =====

    public List<JigLog> searchLogs(LocalDate from, LocalDate to,
                                   JigLogActionType actionType, String username) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt   = to   != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : null;
        String usernameParam = (username == null || username.isBlank()) ? null : username;
        JigLogActionType typeParam = actionType;
        return jigLogRepository.searchLogs(fromDt, toDt, typeParam, usernameParam);
    }

    public List<String> getAllUsernames() {
        return userRepository.findAll().stream()
                .map(User::getUsername)
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<JigStatus, Long> getStatusStats() {
        Map<JigStatus, Long> result = new EnumMap<>(JigStatus.class);
        for (JigStatus s : JigStatus.values()) {
            result.put(s, 0L);
        }
        jigRepository.findAll().forEach(j -> {
            JigStatus s = j.getStatus() != null ? j.getStatus() : JigStatus.Normal;
            result.merge(s, 1L, Long::sum);
        });
        return result;
    }

    public long getTotalJigCount() {
        return jigRepository.count();
    }

    private List<MultipartFile> nonEmptyFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
