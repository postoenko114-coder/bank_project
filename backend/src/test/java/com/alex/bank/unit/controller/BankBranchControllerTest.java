package com.alex.bank.unit.controller;

import com.alex.bank.controllers.BankBranchController;
import com.alex.bank.dto.BankBranchDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BankBranchController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class BankBranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankBranchService bankBranchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getBankBranches_ShouldReturnListOfBranches() throws Exception {
        BankBranchDTO branch1 = new BankBranchDTO();
        branch1.setBankBranchName("Central Branch");

        BankBranchDTO branch2 = new BankBranchDTO();
        branch2.setBankBranchName("West Branch");

        when(bankBranchService.getAllBankBranches()).thenReturn(List.of(branch1, branch2));

        mockMvc.perform(get("/api/v1/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bankBranchService).getAllBankBranches();
    }

    @Test
    @WithMockUser
    void getBankBranch_ShouldReturnBankBranchDTO_WhenBranchExists() throws Exception {
        BankBranchDTO fakeDTO = new BankBranchDTO();
        fakeDTO.setId(1L);
        fakeDTO.setBankBranchName("Central Branch");

        when(bankBranchService.getBankBranchById(1L)).thenReturn(fakeDTO);

        mockMvc.perform(get("/api/v1/branches/{bankBranchId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankBranchName").value("Central Branch"));

        verify(bankBranchService).getBankBranchById(1L);
    }

    @Test
    @WithMockUser
    void getBankBranch_ShouldReturn404_WhenBranchDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"))
                .when(bankBranchService).getBankBranchById(99L);

        mockMvc.perform(get("/api/v1/branches/{bankBranchId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBankBranchesByService_ShouldReturnFilteredBranches() throws Exception {
        BankBranchDTO fakeDTO = new BankBranchDTO();
        fakeDTO.setBankBranchName("Branch With Loans");

        when(bankBranchService.getBranchesByService("Loan")).thenReturn(List.of(fakeDTO));

        mockMvc.perform(get("/api/v1/branches/filter/service")
                        .param("serviceName", "Loan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankBranchService).getBranchesByService("Loan");
    }

    @Test
    @WithMockUser
    void getBankBranchesByLocation_ShouldReturnFilteredBranches() throws Exception {
        BankBranchDTO fakeDTO = new BankBranchDTO();
        fakeDTO.setBankBranchName("London Branch");

        when(bankBranchService.getBranchesByLocation("London", null)).thenReturn(List.of(fakeDTO));

        mockMvc.perform(get("/api/v1/branches/filter/location")
                        .param("city", "London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankBranchService).getBranchesByLocation("London", null);
    }

    @Test
    @WithMockUser
    void getNearestBankBranches_ShouldReturnNearestBranches() throws Exception {
        BankBranchDTO fakeDTO = new BankBranchDTO();
        fakeDTO.setBankBranchName("Nearest Branch");

        when(bankBranchService.getNearestBranches(51.5074, -0.1278)).thenReturn(List.of(fakeDTO));

        mockMvc.perform(get("/api/v1/branches/filter/nearest")
                        .param("latitude", "51.5074")
                        .param("longitude", "-0.1278"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankBranchService).getNearestBranches(51.5074, -0.1278);
    }

    @Test
    @WithMockUser
    void getNearestBankBranches_ShouldReturn400_WhenParamsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/branches/filter/nearest"))
                .andExpect(status().isBadRequest());
    }
}
