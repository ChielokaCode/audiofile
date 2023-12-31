package org.chielokacode.atsa.audiotospeechapi.utils;

import lombok.Data;

import java.util.List;

@Data
public class Transcript {
    private String id;
    private String text;
    private String status;
    private String audio_url;
}
