package org.chielokacode.atsa.audiotospeechapi.controller;

import com.google.gson.Gson;
import org.chielokacode.atsa.audiotospeechapi.serviceImp.AudioToSpeechServiceImpl;
import org.chielokacode.atsa.audiotospeechapi.utils.Api;
import org.chielokacode.atsa.audiotospeechapi.utils.AppConstants;
import org.chielokacode.atsa.audiotospeechapi.utils.Transcript;
import org.chielokacode.atsa.audiotospeechapi.utils.WordsToNumbersUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AudioToSpeechController {
    private Transcript transcript = new Transcript();
    private Gson gson = new Gson();
    HttpResponse<String> getResponse;
    private HttpClient httpClient = HttpClient.newHttpClient();


    private AudioToSpeechServiceImpl audioToSpeechService;

    @Autowired
    public AudioToSpeechController(AudioToSpeechServiceImpl audioToSpeechService) {
        this.audioToSpeechService = audioToSpeechService;
    }

    @PostMapping("/create-request")
    public ResponseEntity<String> createSpeech() throws URISyntaxException, IOException, InterruptedException {



        transcript.setAudio_url(AppConstants.audio_url);
        String audioJson = gson.toJson(transcript);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(AppConstants.URL))
                .header("Authorization", Api.API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(audioJson))
                .build();


        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        transcript = gson.fromJson(postResponse.body(), Transcript.class);

        return new ResponseEntity<>(postResponse.body(), HttpStatus.OK);
    }



    @GetMapping("/get-text")
    public ResponseEntity<String> getSpeech() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(AppConstants.URL + "/" + transcript.getId()))
                .header("Authorization", Api.API_KEY)
                .build();

        while (true) {
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), Transcript.class);

            if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                break;
            }
            Thread.sleep(1000);
        }

        int finalNum = 0;
        WordsToNumbersUtil toNumber = new WordsToNumbersUtil();
        String finalWordToNum = toNumber.convertTextualNumbersInDocument(transcript.getText().replace(".",""));
        String[] splitWords = finalWordToNum.split(" ");
        String[] mathsWords = {"plus", "minus", "times", "divide", "modulus", "-"};

            if(splitWords[1].equals(mathsWords[0])){
                finalNum += Integer.parseInt(splitWords[0]) + Integer.parseInt(splitWords[2]);
            }
        if(splitWords[1].equals(mathsWords[1]) || splitWords[1].equals(mathsWords[5]) ){
            finalNum += Integer.parseInt(splitWords[0]) - Integer.parseInt(splitWords[2]);
        }
        if(splitWords[1].equals(mathsWords[2])){
            finalNum += Integer.parseInt(splitWords[0]) * Integer.parseInt(splitWords[2]);
        }
        if(splitWords[1].equals(mathsWords[3])){
            finalNum += Integer.parseInt(splitWords[0]) / Integer.parseInt(splitWords[2]);
        }
        if(splitWords[1].equals(mathsWords[4])){
            finalNum += Integer.parseInt(splitWords[0]) % Integer.parseInt(splitWords[2]);
        }


        return new ResponseEntity<>("Transcript Completed: " + transcript.getText() + " = " + finalNum, HttpStatus.OK);
    }
    }