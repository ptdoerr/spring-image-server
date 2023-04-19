package com.example.imageserver;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
class SpringDemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ImageServerApplication imageServerApp;
	
	@Test
	void contextLoads() {
		assertThat(imageServerApp).isNotNull();
	}

	private String maps_list_output = 
		"[\"current-cases.jpg\",\"current-graph.jpg\",\"current-map.jpg\"]";

	@WithMockUser(value = "spring")
	@Test
	public void shouldReturnImageFilesList() throws Exception {
		imageServerApp.setImageDir("src/test/resources/static/images");

		System.out.println("In shouldReturnImageFilesList()");

		File f = new File(imageServerApp.getImageDir());
		System.out.println("dir = " +f.getPath());
		System.out.println("files = " +Arrays.toString(f.list()));

		MvcResult result = this.mockMvc.perform(get("/maps-list"))
					//.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().string(maps_list_output))
					.andReturn();

		System.out.println("Output: " +result.getResponse().getContentAsString());
	}

	@WithMockUser(value = "spring")
	@Test
	public void shouldReturnImage() throws Exception {
		imageServerApp.setImageDir("src/test/resources/static/images");

		String imageFname = "current-map.jpg";

		System.out.println("In shouldReturnImage()");

		this.mockMvc.perform(get("/map-image-file?fname=" +imageFname))
					//.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType("image/jpeg"))
					.andReturn();
	}

}
