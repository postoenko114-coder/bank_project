package com.example.demo.unit.controller.client;

import com.example.demo.controllers.client.ReservationController;
import com.example.demo.dto.ReservationDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.reservation.StatusReservation;
import com.example.demo.security.JwtService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.example.demo.services.reservation.ReservationService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ReservationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private BankServiceService bankServiceService;

    @MockitoBean
    private BankBranchService bankBranchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private ReservationDTO buildReservationDTO(Long id, StatusReservation status) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(id);
        dto.setStatusReservation(status);
        dto.setServiceName("Consultation");
        dto.setBranchName("Central Branch");
        dto.setStartReservation(LocalDateTime.now().plusDays(1));
        dto.setEndReservation(LocalDateTime.now().plusDays(1).plusMinutes(30));
        return dto;
    }

    @Test
    @WithMockUser
    void getReservations_ShouldReturnReservationDTOList_WhenUserExists() throws Exception {
        when(reservationService.getAllReservationsOfUser(1L))
                .thenReturn(List.of(buildReservationDTO(1L, StatusReservation.ACTIVE),
                        buildReservationDTO(2L, StatusReservation.COMPLETED)));

        mockMvc.perform(get("/api/v1/{userId}/reservations", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(reservationService).getAllReservationsOfUser(1L);
    }

    @Test
    @WithMockUser
    void getReservations_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(reservationService).getAllReservationsOfUser(99L);

        mockMvc.perform(get("/api/v1/{userId}/reservations", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getReservation_ShouldReturnReservationDTO_WhenReservationExists() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(buildReservationDTO(1L, StatusReservation.ACTIVE));

        mockMvc.perform(get("/api/v1/{userId}/reservations/{reservationId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(reservationService).getReservationById(1L);
    }

    @Test
    @WithMockUser
    void getReservation_ShouldReturn404_WhenReservationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"))
                .when(reservationService).getReservationById(99L);

        mockMvc.perform(get("/api/v1/{userId}/reservations/{reservationId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnReservationDTO_WhenDataIsValid() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setBankBranchName("Central Branch");

        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));
        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(reservationService.addReservation(any(LocalDateTime.class), eq(1L), eq(fakeService), eq(fakeBranch)))
                .thenReturn(buildReservationDTO(1L, StatusReservation.ACTIVE));

        mockMvc.perform(post("/api/v1/{userId}/reservations", 1L)
                        .param("startReservation", LocalDateTime.now().plusDays(1).toString())
                        .param("serviceName", "Consultation")
                        .param("branchName", "Central Branch"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusReservation").value("ACTIVE"));

        verify(reservationService).addReservation(any(), eq(1L), any(), any());
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturn409_WhenTimeSlotIsTaken() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));
        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "This time slot is already taken"))
                .when(reservationService).addReservation(any(), anyLong(), any(), any());

        mockMvc.perform(post("/api/v1/{userId}/reservations", 1L)
                        .param("startReservation", LocalDateTime.now().plusDays(1).toString())
                        .param("serviceName", "Consultation")
                        .param("branchName", "Central Branch"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void cancelReservation_ShouldReturnCancelledReservation_WhenReservationExists() throws Exception {
        when(reservationService.cancelReservation(1L)).thenReturn(buildReservationDTO(1L, StatusReservation.CANCELLED));

        mockMvc.perform(put("/api/v1/{userId}/reservations/{reservationId}/cancel", 1L, 1L))
                .andExpect(status().isOk());

        verify(reservationService).cancelReservation(1L);
    }

    @Test
    @WithMockUser
    void cancelReservation_ShouldReturn404_WhenReservationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"))
                .when(reservationService).cancelReservation(99L);

        mockMvc.perform(put("/api/v1/{userId}/reservations/{reservationId}/cancel", 1L, 99L))
                .andExpect(status().isNotFound());
    }
}