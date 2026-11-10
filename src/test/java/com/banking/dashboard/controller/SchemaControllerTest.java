package com.banking.dashboard.controller;

import com.banking.dashboard.dto.SchemaTableInfo;
import com.banking.dashboard.service.SchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemaController.class)
@ActiveProfiles("test")
class SchemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SchemaService schemaService;

    @Test
    void getTables_returnsSchemaInfo() throws Exception {
        SchemaTableInfo info = new SchemaTableInfo(
                "customers", "BASE TABLE", "customer_id", "integer", "NO", 1);

        when(schemaService.getSchemaTablesAndColumns()).thenReturn(List.of(info));

        mockMvc.perform(get("/api/schema/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].tableName", is("customers")))
                .andExpect(jsonPath("$.data[0].tableType", is("BASE TABLE")))
                .andExpect(jsonPath("$.data[0].columnName", is("customer_id")))
                .andExpect(jsonPath("$.data[0].dataType", is("integer")))
                .andExpect(jsonPath("$.count", is(1)));
    }

    @Test
    void getTables_returnsEmptyList() throws Exception {
        when(schemaService.getSchemaTablesAndColumns()).thenReturn(List.of());

        mockMvc.perform(get("/api/schema/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.count", is(0)));
    }
}
