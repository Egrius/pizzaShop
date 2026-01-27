package by.egrius.pizzaShop.integration.controller;

import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.repository.PizzaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicPizzaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class GetPizzaCardsTests {

        @BeforeEach
        void setUp() {
            pizzaRepository.deleteAll();

            List<Pizza> pizzas = List.of(
                    Pizza.create(
                            "Маргарита",
                            "Классическая пицца",
                            "/images/margherita.jpg",
                            "Классические",
                            true,
                            15
                    ),
                    Pizza.create(
                            "Пицца_2",
                            "Классическая пицца_2",
                            "/images/margherita.jpg",
                            "Классические",
                            true,
                            15
                    )
            );
            pizzaRepository.saveAll(pizzas);
        }

        @Test
        void getPizzaCards_shouldReturnPaginatedPizzas() throws Exception {

            mockMvc.perform(get("/api/pizzas/cards")
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("Маргарита"))
                    .andExpect(jsonPath("$.content[1].name").value("Пицца_2"));
        }

        @Test
        void getPizzaCards_shouldReturnPaginatedPizzasWithDefaultParams() throws Exception {
            mockMvc.perform(get("/api/pizzas/cards"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("Маргарита"))
                    .andExpect(jsonPath("$.content[1].name").value("Пицца_2"));
        }

        @ParameterizedTest
        @CsvSource({

                "-1, 10",
                "0, 0",
                "0, -5",
                "abc, 10",
                "0, abc"
        })
        void getPizzaCards_shouldThrow400_whenBadPageParameters(String page, String pageSize) throws Exception{

            mockMvc.perform(get("/api/pizzas/cards")
                            .param("page", page)
                            .param("pageSize", pageSize)
                    )
                    .andExpect(content().contentType(MediaType.parseMediaType("application/json"))) // -> поправить тип в хэндлере и исправить тест
                    .andExpect(status().isBadRequest());
        }



    }

}