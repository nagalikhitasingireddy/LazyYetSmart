package com.transcriber.audiotranscriber.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TranscriberController {
private ChatClient chatClient;
	
	public TranscriberController(OllamaChatModel chatModel) {
		this.chatClient=ChatClient.create(chatModel);
	}

	private String latestTranscription = "";  // store transcription for summarizing

    @GetMapping("/realWeb")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String handleFormUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select an audio file to upload.");
            return "index";
        }

        try {
            // Step 1: Save the uploaded file
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // create uploads folder if not exists
            }

            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Step 2: Call Python script to transcribe using Whisper
            ProcessBuilder pb = new ProcessBuilder("python", "transcribe.py", filePath.toString());
            pb.redirectErrorStream(true); // combine stderr with stdout
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder transcription = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip warning messages
                if (line.contains("UserWarning") || line.contains("FP16") || line.contains("Lib\\site-packages")) {
                    continue;
                }
                transcription.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                model.addAttribute("error", "Transcription failed.");
                return "index";
            }

            latestTranscription = transcription.toString().trim(); // store result
            
            model.addAttribute("transcription", latestTranscription);
            model.addAttribute("showSummarizeButton", true);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
        }

        return "index";
    }

	
   
    @GetMapping
    public String homepage() {
    	return "home";
    }
    @GetMapping("/summary")
    public String getAnswer(Model model) {
        model.addAttribute("transcription", latestTranscription);
        String message = "summarize this total text," + latestTranscription;

        try {
            ChatResponse chatResponse = chatClient.prompt(message).call().chatResponse();
            String response = chatResponse.getResult().getOutput().getText();
            model.addAttribute("response", response);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", " I wanted to give you a cool AI summarization tool, but turns out... LLaMA’s got a big appetite for RAM 🐑💻. This beast doesn’t even blink until it gets what it wants — LOTS of memory.\r\n"
            		+ "\r\n"
            		+ "And why LLaMA, you ask?\r\n"
            		+ "Well... let's just say \"empty pockets teach the best lessons\" 😅💸. OpenAI is amazing, but my wallet looked at the pricing and fainted on the spot. So I went the free and open-source route like a true broke techie with ambition.\r\n"
            		+ "\r\n"
            		+ "✨ BUT good news!\r\n"
            		+ "If you download the full code and install Ollama (the backstage muscle behind the scenes), everything should run like butter 🍞🔥. Just feed your system enough RAM and boom — summarization magic unlocked.\r\n"
            		+ "\r\n"
            		+ "So go ahead, give it a shot! Trust me, it's worth the extra clicks. And hey — if you're also running low on funds, you’ll feel this journey in your soul. 😂💀\r\n"
            		+ "\r\n"
            		+ "Team Broke, but Building. 💻💪");
        }

        return "index";
    }

}
