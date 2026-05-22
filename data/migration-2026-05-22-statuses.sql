USE web_fixture_management;

ALTER TABLE jigs
  DROP CHECK chk_jigs_status;

ALTER TABLE jigs
  ADD CONSTRAINT chk_jigs_status CHECK (
    status IN ('OnProcess', 'Normal', 'Scrap', 'Hold', 'Repair', 'Maintain')
  );
