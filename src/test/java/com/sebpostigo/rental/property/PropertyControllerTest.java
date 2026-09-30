package com.sebpostigo.rental.property;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sebpostigo.rental.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PropertyControllerTest extends IntegrationTest {

	@Test
	void createPropertyRequiresAuth() throws Exception {
		mvc.perform(post("/properties").contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Casa Sol\",\"address\":\"Calle 1\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void createPropertyReturnsCreatedPropertyWithDefaults() throws Exception {
		Cookie ana = signUp("ana@example.com");

		mvc.perform(post("/properties").cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Casa Sol\",\"address\":\"Calle 1\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value("Casa Sol"))
			.andExpect(jsonPath("$.address").value("Calle 1"))
			.andExpect(jsonPath("$.property_type").value("apartment"))
			.andExpect(jsonPath("$.currency").value("USD"));
	}

	@Test
	void listPropertiesReturnsOnlyOwn() throws Exception {
		Cookie ana = signUp("ana@example.com");
		Cookie ben = signUp("ben@example.com");
		createProperty(ana, "Casa Sol");
		createProperty(ana, "Depa Luna");
		createProperty(ben, "Ben House");

		mvc.perform(get("/properties").cookie(ana))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].name", contains("Casa Sol", "Depa Luna")));
	}

	@Test
	void getPropertyById() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long id = createProperty(ana, "Casa Sol");

		mvc.perform(get("/properties/{id}", id).cookie(ana))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Casa Sol"));
	}

	@Test
	void getOtherUsersPropertyReturns404() throws Exception {
		Cookie ana = signUp("ana@example.com");
		Cookie ben = signUp("ben@example.com");
		long id = createProperty(ana, "Casa Sol");

		mvc.perform(get("/properties/{id}", id).cookie(ben))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Property not found"));
	}

	@Test
	void updatePropertyChangesOnlyProvidedFields() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long id = createProperty(ana, "Casa Sol");

		mvc.perform(patch("/properties/{id}", id).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Casa Luna\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Casa Luna"))
			.andExpect(jsonPath("$.address").value("Av. Test 123"));
	}

	@Test
	void deleteProperty() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long id = createProperty(ana, "Casa Sol");

		mvc.perform(delete("/properties/{id}", id).cookie(ana)).andExpect(status().isNoContent());

		mvc.perform(get("/properties/{id}", id).cookie(ana)).andExpect(status().isNotFound());
	}

	@Test
	void deleteOtherUsersPropertyReturns404() throws Exception {
		Cookie ana = signUp("ana@example.com");
		Cookie ben = signUp("ben@example.com");
		long id = createProperty(ana, "Casa Sol");

		mvc.perform(delete("/properties/{id}", id).cookie(ben)).andExpect(status().isNotFound());

		mvc.perform(get("/properties/{id}", id).cookie(ana)).andExpect(status().isOk());
	}

}
