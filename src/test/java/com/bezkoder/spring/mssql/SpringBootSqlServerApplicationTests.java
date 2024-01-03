package com.bezkoder.spring.mssql;

import com.bezkoder.spring.mssql.controller.TutorialController;
import com.bezkoder.spring.mssql.model.Tutorial;
import com.bezkoder.spring.mssql.repository.TutorialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringBootSqlServerApplicationTests {

	@InjectMocks
	private TutorialController tutorialController;

	private MockMvc mockMvc;
	@Mock
	private TutorialRepository tutorialRepository;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(tutorialController).build();
	}

	@Test
	void testGetTutorial() throws Exception {
		Tutorial tutorial = new Tutorial("first tutorial title", "dummy data", false);

		Mockito.when(tutorialRepository.findById(tutorial.getId())).thenReturn(Optional.of(tutorial));

		mockMvc.perform(get("/api/tutorials/" + tutorial.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("first tutorial title")))
				.andExpect(jsonPath("$.description", is("dummy data")))
				.andExpect(jsonPath("$.published", is(false)));
	}

	@Test
	void testGetNonExistentTutorial() throws Exception {
		Mockito.when(tutorialRepository.findById(100L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/tutorials/100"))
				.andExpect(status().isNotFound());
	}


	@Test
	void testCreateTutorial() throws Exception {
		Tutorial tutorial = new Tutorial("new tutorial created", "dummy data", false);

		Mockito.when(tutorialRepository.save(Mockito.any(Tutorial.class))).thenReturn(tutorial);

		mockMvc.perform(post("/api/tutorials")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(tutorial)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("new tutorial created")));
	}

	@Test
	void testCreateTutorialWithInvalidData() throws Exception {
		Tutorial invalidTutorial = new Tutorial();

		mockMvc.perform(post("/api/tutorials")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(invalidTutorial)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testUpdateTutorial() throws Exception {
		Tutorial existingTutorial = new Tutorial("tutorial to be updated", "dummy data", false);
		Tutorial updatedTutorial = new Tutorial("updated title", "updated data", true);

		Mockito.when(tutorialRepository.findById(existingTutorial.getId())).thenReturn(Optional.of(existingTutorial));
		Mockito.when(tutorialRepository.save(Mockito.any(Tutorial.class))).thenReturn(updatedTutorial);

		mockMvc.perform(put("/api/tutorials/" + existingTutorial.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(updatedTutorial)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("updated title")))
				.andExpect(jsonPath("$.published", is(true)));
	}

	@Test
	void testUpdateNonExistentTutorial() throws Exception {
		Tutorial updatedTutorial = new Tutorial("updated title", "updated data", true);

		Mockito.when(tutorialRepository.findById(updatedTutorial.getId())).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/tutorials/" + updatedTutorial.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(updatedTutorial)))
				.andExpect(status().isNotFound());
	}

	@Test
	void testUpdateTutorialWithInvalidData() throws Exception {
		Tutorial existingTutorial = new Tutorial("tutorial to be updated", "dummy data", false);
		Tutorial invalidTutorial = new Tutorial("too looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
				+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
				+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
				+ "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
				+ "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong",
				"updated data", true);

		Mockito.when(tutorialRepository.findById(existingTutorial.getId())).thenReturn(Optional.of(existingTutorial));
		Mockito.when(tutorialRepository.save(Mockito.any(Tutorial.class))).thenReturn(invalidTutorial);

		mockMvc.perform(put("/api/tutorials/" + existingTutorial.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(invalidTutorial)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Title is too long")));
	}

	@Test
	void testDeleteTutorial() throws Exception {
		Tutorial existingTutorial = new Tutorial("tutorial to be delete", "dummy data", false);
		Mockito.when(tutorialRepository.findById(existingTutorial.getId())).thenReturn(Optional.of(existingTutorial));
		Mockito.doNothing().when(tutorialRepository).deleteById(existingTutorial.getId());

		mockMvc.perform(delete("/api/tutorials/" + existingTutorial.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void testDeleteNonExistentTutorial() throws Exception {
		Mockito.doThrow(new EmptyResultDataAccessException(1)).when(tutorialRepository).deleteById(500L);

		mockMvc.perform(delete("/api/tutorials/500"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testDeleteAllTutorials() throws Exception {
		Mockito.doNothing().when(tutorialRepository).deleteAll();

		mockMvc.perform(delete("/api/tutorials"))
				.andExpect(status().isNoContent());
	}

	@Test
	void testListPublishedTutorials() throws Exception {
		Tutorial tutorial = new Tutorial("tutorial in db", "dummy description", true);
		Mockito.when(tutorialRepository.findByPublished(true)).thenReturn(Collections.singletonList(tutorial));

		mockMvc.perform(get("/api/tutorials/published"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].title", is("tutorial in db")));;
	}
}
