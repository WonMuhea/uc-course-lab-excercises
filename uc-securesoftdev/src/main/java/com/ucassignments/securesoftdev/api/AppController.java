package com.ucassignments.securesoftdev.api;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Controller
public class AppController {

    @GetMapping("/")
    public String index(Model model) throws IOException {
        // Read and parse README.md
        ClassPathResource resource = new ClassPathResource("README.md");
        String markdownContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            markdownContent = reader.lines().collect(Collectors.joining("\n"));
        }

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        model.addAttribute("readmeHtml", htmlContent);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}

