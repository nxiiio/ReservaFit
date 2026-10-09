package cl.nxiiio.ReservaFit_Backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// Allowed origins come from app.cors.allowed-origins (env CORS_ALLOWED_ORIGINS in Docker)
class CorsConfigTest {

    private static ResultActions preflight(MockMvc mockMvc, String origin) throws Exception {
        return mockMvc.perform(options("/api/reservas")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"));
    }

    // Assumes CORS_ALLOWED_ORIGINS is not set in the environment running the tests
    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class Defaults {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void allowsTheViteDevServer() throws Exception {
            preflight(mockMvc, "http://localhost:5173")
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        }

        @Test
        void rejectsOtherOrigins() throws Exception {
            preflight(mockMvc, "http://evil.example")
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
    }

    // The space after the comma checks that entries are trimmed
    @Nested
    @SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000, http://127.0.0.1:5173")
    @AutoConfigureMockMvc
    class FromProperty {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void allowsEveryConfiguredOrigin() throws Exception {
            preflight(mockMvc, "http://localhost:3000")
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
            preflight(mockMvc, "http://127.0.0.1:5173")
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"));
        }

        @Test
        void replacesTheDefaultOrigin() throws Exception {
            preflight(mockMvc, "http://localhost:5173")
                    .andExpect(status().isForbidden());
        }
    }
}
