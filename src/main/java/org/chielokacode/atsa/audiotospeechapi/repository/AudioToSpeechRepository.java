package org.chielokacode.atsa.audiotospeechapi.repository;

import org.chielokacode.atsa.audiotospeechapi.model.AudioToSpeech;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioToSpeechRepository extends JpaRepository<AudioToSpeech, Long> {
}
