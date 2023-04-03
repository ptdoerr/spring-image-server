package com.example.imageserver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.core.ResponseBytes;

//import com.example.imageserver.*;

@SpringBootApplication
@RestController
public class ImageServerApplication  {
	//public class ImageServerApplication extends WebSecurityConfigurerAdapter {

	private String imageDir = "C:/Users/phild/Documents/GitLab/COVIDdashboard/data/map-images";
	private String s3ImageBucket = "covid-dash-ptd";
	public static final String CONTENT_TYPE = "Content-Type";
     public static final String CONTENT_LENGTH = "Content-Length";
     public static final String IMAGE_CONTENT = "image/";
	private S3Client s3 = null ;

	public static void main(String[] args) {
		SpringApplication.run(ImageServerApplication.class, args);
	}

	private S3Client getClient() {
		System.out.println("Getting S3 Client");

        // Create the S3Client object.
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();

        return s3;
     }

	@GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
		System.out.println("Principal Name : " +principal.getAttribute("login"));
		System.out.println("Principal : " +principal);
        return Collections.singletonMap("name", principal.getAttribute("login"));
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

	@CrossOrigin(origins = "http://localhost:8081")
	@GetMapping("/s3-maps-list")
	public String s3MapsList() {
		String[] pathnames = new String[] { "No Files Found"};
		Gson gson = new Gson();
		ArrayList<String> filesList = new ArrayList<String>();
		System.out.println("in s3MapsList, getting client");
		s3 = getClient();

		System.out.println("in s2-maps-list()");
       	try {
			System.out.println("Getting objects for bucket: " +s3ImageBucket);
          	ListObjectsRequest listObjects = ListObjectsRequest
                  .builder()
                  .bucket(s3ImageBucket)
                  .build();

          	ListObjectsResponse res = s3.listObjects(listObjects);
          	List<S3Object> objects = res.contents();

         	for (S3Object myValue: objects) {
				
              	String key = myValue.key(); // We need the key to get the tags.
				System.out.println("found object: " +key);
				filesList.add(key);

			}
		} catch (S3Exception e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			//System.exit(1);
		  }

		pathnames = filesList.toArray(new String[0]);

		if(pathnames != null) {
			return gson.toJson(pathnames);
		} else {
			return gson.toJson("No Files Found");
		}	
	}

	@CrossOrigin(origins = "http://localhost:8081")
	@GetMapping("/s3-map-image-file")
	@ResponseBody
	public ResponseEntity<byte[]> s3mapImageFile(@RequestParam(value = "fname") String fname) {
		MediaType contentType = MediaType.IMAGE_JPEG;
		FileInputStream in = null;
		System.out.println("Image filename: " +fname);

		String objectKey = fname;
		s3 = getClient();

		try {
			// create a GetObjectRequest instance.
			GetObjectRequest objectRequest = GetObjectRequest
				.builder()
				.key(objectKey)
				.bucket(s3ImageBucket)
				.build();

            // get the byte[] from this AWS S3 object and return a ResponseEntity.
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
			return ResponseEntity.status(HttpStatus.OK)
                    .contentType(contentType)
                    .header(CONTENT_LENGTH, String.valueOf(objectBytes.asByteArray().length))
                    .body(objectBytes.asByteArray());

		} catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            //System.exit(1);
		}


		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

	}

/* 
	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	// @formatter:off
        http
            .authorizeRequests(a -> a
                .antMatchers("/", "/error", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .oauth2Login();
        // @formatter:on
    }
*/
}
