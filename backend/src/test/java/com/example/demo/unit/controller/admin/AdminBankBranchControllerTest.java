package com.example.demo.unit.controller.admin;

import com.example.demo.controllers.admin.AdminBankBranchController;
import com.example.demo.dto.BankBranchDTO;
import com.example.demo.dto.LocationDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.security.JwtService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminBankBranchController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminBankBranchControllerTest {

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

    private BankBranchDTO buildBranchDTO(Long id, String name) {
        BankBranchDTO dto = new BankBranchDTO();
        dto.setId(id);
        dto.setBankBranchName(name);
        dto.setLocationDTO(new LocationDTO());
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBankBranches_ShouldReturnBranchDTOList() throws Exception {
        when(bankBranchService.getAllBankBranches())
                .thenReturn(List.of(buildBranchDTO(1L, "Central"), buildBranchDTO(2L, "West")));

        mockMvc.perform(get("/api/v1/admin/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bankBranchService).getAllBankBranches();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBankBranchById_ShouldReturnBranchDTO_WhenBranchExists() throws Exception {
        when(bankBranchService.getBankBranchById(1L)).thenReturn(buildBranchDTO(1L, "Central Branch"));

        mockMvc.perform(get("/api/v1/admin/branches/{bankBranchId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankBranchName").value("Central Branch"));

        verify(bankBranchService).getBankBranchById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBankBranchById_ShouldReturn404_WhenBranchDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"))
                .when(bankBranchService).getBankBranchById(99L);

        mockMvc.perform(get("/api/v1/admin/branches/{bankBranchId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBankBranch_ShouldReturnBranchDTO_WhenDataIsValid() throws Exception {
        BankBranchDTO fakeRequest = buildBranchDTO(null, "New Branch");

        when(bankBranchService.addBankBranch(any(BankBranchDTO.class), any(LocationDTO.class)))
                .thenReturn(buildBranchDTO(1L, "New Branch"));

        mockMvc.perform(post("/api/v1/admin/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bankBranchName").value("New Branch"));

        verify(bankBranchService).addBankBranch(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editBankBranch_ShouldReturnUpdatedBranchDTO_WhenBranchExists() throws Exception {
        BankBranchDTO fakeRequest = buildBranchDTO(1L, "Updated Branch");

        when(bankBranchService.updateBankBranch(eq(1L), any(BankBranchDTO.class)))
                .thenReturn(buildBranchDTO(1L, "Updated Branch"));

        mockMvc.perform(put("/api/v1/admin/branches/{bankBranchId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankBranchName").value("Updated Branch"));

        verify(bankBranchService).updateBankBranch(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editBankBranch_ShouldReturn404_WhenBranchDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"))
                .when(bankBranchService).updateBankBranch(eq(99L), any());

        mockMvc.perform(put("/api/v1/admin/branches/{bankBranchId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBranchDTO(99L, "Ghost"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addServiceToBranch_ShouldReturnUpdatedBranchDTO_WhenBothExist() throws Exception {
        when(bankBranchService.addBankServiceToBranch(1L, 1L))
                .thenReturn(buildBranchDTO(1L, "Central Branch"));

        mockMvc.perform(put("/api/v1/admin/branches/{bankBranchId}/services", 1L)
                        .param("serviceId", "1"))
                .andExpect(status().isOk());

        verify(bankBranchService).addBankServiceToBranch(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addServiceToBranch_ShouldReturn404_WhenServiceNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"))
                .when(bankBranchService).addBankServiceToBranch(1L, 99L);

        mockMvc.perform(put("/api/v1/admin/branches/{bankBranchId}/services", 1L)
                        .param("serviceId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteServiceFromBranch_ShouldReturn204_WhenBothExist() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/branches/{bankBranchId}/services", 1L)
                        .param("serviceId", "1"))
                .andExpect(status().isNoContent());

        verify(bankBranchService).deleteBankServiceFromBranch(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBankBranch_ShouldReturn204_WhenBranchExists() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/branches/{bankBranchId}", 1L))
                .andExpect(status().isNoContent());

        verify(bankBranchService).deleteBankBranch(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBankBranch_ShouldReturn404_WhenBranchDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch not found"))
                .when(bankBranchService).deleteBankBranch(99L);

        mockMvc.perform(delete("/api/v1/admin/branches/{bankBranchId}", 99L))
                .andExpect(status().isNotFound());
    }
}