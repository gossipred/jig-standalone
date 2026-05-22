package com.jj.jig.jig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record JigNumber(String jigNo, String jigBaseNo, String setNo) {

    private static final Pattern JIG_NO_PATTERN = Pattern.compile("^([A-Z]{2}-[A-Z]{2}-[0-9]{3})(?:-([0-9]{2}))?$");

    public static JigNumber parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Jig No. is required.");
        }

        String normalizedInput = value.trim().toUpperCase();
        Matcher matcher = JIG_NO_PATTERN.matcher(normalizedInput);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Jig No. format must be like AM-ME-001 or AM-ME-001-02.");
        }

        String baseNo = matcher.group(1);
        String setNo = matcher.group(2);
        if ("01".equals(setNo)) {
            return new JigNumber(baseNo, baseNo, null);
        }

        String jigNo = setNo == null ? baseNo : baseNo + "-" + setNo;
        return new JigNumber(jigNo, baseNo, setNo);
    }
}
