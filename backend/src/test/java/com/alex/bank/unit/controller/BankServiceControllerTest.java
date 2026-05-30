package com.alex.bank.unit.controller;

import com.alex.bank.controllers.BankServiceController;
import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.mapper.BankServiceMapperImpl;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.bankService.BankServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(controllers = {BankServiceController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(BankServiceMapperImpl.class)
public class BankServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankServiceService bankServiceService;

    @MockitoBean
    private BankBranchService bankBranchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAllBankServices_ShouldReturnListOfServices() throws Exception {
        BankServiceDTO s1 = new BankServiceDTO();
        s1.setBankServiceName("Loan");

        BankServiceDTO s2 = new BankServiceDTO();
        s2.setBankServiceName("Card Issue");

        when(bankServiceService.getServicesList()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bankServiceService).getServicesList();
    }

    @Test
    @WithMockUser
    void getBankService_ShouldReturnBankServiceDTO_WhenServiceExists() throws Exception {
        BankServiceDTO fakeDTO = new BankServiceDTO();
        fakeDTO.setId(1L);
        fakeDTO.setBankServiceName("Loan");
        fakeDTO.setDuration("30 min");

        when(bankServiceService.getServiceById(1L)).thenReturn(fakeDTO);

        mockMvc.perform(get("/api/v1/services/{bankServiceId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankServiceName").value("Loan"));

        verify(bankServiceService).getServiceById(1L);
    }

    @Test
    @WithMockUser
    void getBankService_ShouldReturn404_WhenServiceDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"))
                .when(bankServiceService).getServiceById(99L);

        mockMvc.perform(get("/api/v1/services/{bankServiceId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBankServiceByName_ShouldReturnFilteredServices() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Loan");
        fakeService.setDuration("30 min");
        fakeService.setDescription("Loan consultation");

        when(bankServiceService.findServiceByName("Loan")).thenReturn(List.of(fakeService));

        mockMvc.perform(get("/api/v1/services/filter/name")
                        .param("name", "Loan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankServiceService).findServiceByName("Loan");
    }

    @Test
    @WithMockUser
    void getBankServiceByName_ShouldReturn400_WhenNameParamMissing() throws Exception {
        mockMvc.perform(get("/api/v1/services/filter/name"))
                .andExpect(status().isBadRequest());
    }
}