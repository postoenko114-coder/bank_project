package com.example.demo.unit.controller.admin;

import com.example.demo.controllers.admin.AdminBankServiceController;
import com.example.demo.dto.BankServiceDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.mapper.BankServiceMapperImpl;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.security.JwtService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminBankServiceController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(BankServiceMapperImpl.class)
public class AdminBankServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankServiceService bankServiceService;

    @MockitoBean
    private BankBranchService bankBranchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private BankServiceDTO buildServiceDTO(Long id, String name) {
        BankServiceDTO dto = new BankServiceDTO();
        dto.setId(id);
        dto.setBankServiceName(name);
        dto.setDuration("30 min");
        dto.setDescription("Test description");
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBankServices_ShouldReturnServiceDTOList() throws Exception {
        when(bankServiceService.getServicesList())
                .thenReturn(List.of(buildServiceDTO(1L, "Loan"), buildServiceDTO(2L, "Card Issue")));

        mockMvc.perform(get("/api/v1/admin/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bankServiceService).getServicesList();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBankServiceById_ShouldReturnServiceDTO_WhenServiceExists() throws Exception {
        when(bankServiceService.getServiceById(1L)).thenReturn(buildServiceDTO(1L, "Loan"));

        mockMvc.perform(get("/api/v1/admin/services/{bankServiceId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankServiceName").value("Loan"));

        verify(bankServiceService).getServiceById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBankServiceById_ShouldReturn404_WhenServiceDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"))
                .when(bankServiceService).getServiceById(99L);

        mockMvc.perform(get("/api/v1/admin/services/{bankServiceId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBankServiceByName_ShouldReturnFilteredServices() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Loan");
        fakeService.setDuration("30 min");
        fakeService.setDescription("Loan consultation");

        when(bankServiceService.findServiceByName("Loan")).thenReturn(List.of(fakeService));

        mockMvc.perform(get("/api/v1/admin/services/filter/name")
                        .param("serviceName", "Loan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankServiceService).findServiceByName("Loan");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailabilityServiceOnDate_ShouldReturnAvailable_WhenSlotsExist() throws Exception {
        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setBankBranchName("Central Branch");

        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(bankServiceService.getAvailabilityServiceByDate(1L, 1L, LocalDate.of(2025, 6, 1))).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/services/{bankServiceId}/availability", 1L)
                        .param("branchName", "Central Branch")
                        .param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.message").value("Service available"));

        verify(bankServiceService).getAvailabilityServiceByDate(1L, 1L, LocalDate.of(2025, 6, 1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailabilityServiceOnDate_ShouldReturnNotAvailable_WhenAllSlotsTaken() throws Exception {
        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(bankServiceService.getAvailabilityServiceByDate(1L, 1L, LocalDate.of(2025, 6, 1))).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/services/{bankServiceId}/availability", 1L)
                        .param("branchName", "Central Branch")
                        .param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false))
                .andExpect(jsonPath("$.message").value("Service not available"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBankService_ShouldReturnServiceDTO_WhenDataIsValid() throws Exception {
        BankServiceDTO fakeRequest = buildServiceDTO(null, "New Service");

        when(bankServiceService.addService(any(BankServiceDTO.class)))
                .thenReturn(buildServiceDTO(1L, "New Service"));

        mockMvc.perform(post("/api/v1/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankServiceName").value("New Service"));

        verify(bankServiceService).addService(any(BankServiceDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editService_ShouldReturnUpdatedServiceDTO_WhenServiceExists() throws Exception {
        BankServiceDTO fakeRequest = buildServiceDTO(1L, "Updated Service");

        when(bankServiceService.updateService(eq(1L), any(BankServiceDTO.class)))
                .thenReturn(fakeRequest);

        mockMvc.perform(put("/api/v1/admin/services/{bankServiceId}/update", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankServiceName").value("Updated Service"));

        verify(bankServiceService).updateService(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editService_ShouldReturn404_WhenServiceDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"))
                .when(bankServiceService).updateService(eq(99L), any());

        mockMvc.perform(put("/api/v1/admin/services/{bankServiceId}/update", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildServiceDTO(99L, "Ghost"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteService_ShouldReturn204_WhenServiceExists() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/services/{bankServiceId}", 1L))
                .andExpect(status().isNoContent());

        verify(bankServiceService).deleteService(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteService_ShouldReturn404_WhenServiceDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"))
                .when(bankServiceService).deleteService(99L);

        mockMvc.perform(delete("/api/v1/admin/services/{bankServiceId}", 99L))
                .andExpect(status().isNotFound());
    }
}