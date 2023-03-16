package com.example.imageserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.springframework.core.io.InputStreamResource;
import com.google.gson.Gson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
@RestController
public class ImageServerApplication {

	private String imageDir = "C:/Users/phild/Documents/GitLab/COVIDdashboard/data/map-images";

	public static void main(String[] args) {
		SpringApplication.run(ImageServerApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@CrossOrigin(origins = "http://localhost:8081")
	@GetMapping("/maps-list")
	public String mapsList() {
		String[] pathnames = new String[] { "No Files Found"};
		Gson gson = new Gson();


		try {
			File f = new File(imageDir);
			System.out.printf("dir = " +f.getPath());
			System.out.printf("files = " +f.list());
			pathnames = f.list();
			
		} catch (Exception e) {
			pathnames[0] = "No Files Found";
		}

		if(pathnames != null) {
			return gson.toJson(pathnames);
		} else {
			return gson.toJson("No Files Found");
		}	
	}

	@CrossOrigin(origins = "http://localhost:8081")
	@GetMapping("/map-image-file")
	@ResponseBody
	public ResponseEntity<InputStreamResource> mapImageFile(@RequestParam(value = "fname") String fname) {
		MediaType contentType = MediaType.IMAGE_JPEG;
		FileInputStream in = null;
		System.out.println("Image filename: " +fname);


		String imagePath = imageDir +"/" +fname;
		System.out.println("Looking for image: " +imagePath);
		
		try {
			in = new FileInputStream(imagePath);
		} catch (FileNotFoundException ex) {
			System.err.println(imagePath +": Not Found");
		}

		if(in != null) {
			return ResponseEntity.ok()
      			.contentType(contentType)
      			.body(new InputStreamResource(in));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

}
