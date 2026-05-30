package com.alex.bank.unit.controller.admin;

import com.alex.bank.controllers.admin.AdminReservationController;
import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.models.branch.reservation.StatusReservation;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.bankService.BankServiceService;
import com.alex.bank.services.reservation.ReservationService;
import com.alex.bank.services.user.UserService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminReservationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private BankBranchService bankBranchService;

    @MockitoBean
    private BankServiceService bankServiceService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void getReservations_ShouldReturnAllReservations() throws Exception {
        when(reservationService.getAllReservations())
                .thenReturn(List.of(buildReservationDTO(1L, StatusReservation.ACTIVE),
                        buildReservationDTO(2L, StatusReservation.ACTIVE)));

        mockMvc.perform(get("/api/v1/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(reservationService).getAllReservations();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReservationsByFilters_ShouldReturnFilteredReservations() throws Exception {
        LocalDate fakeDate = LocalDate.now().minusDays(1);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(10L);

        BankService fakeService = new BankService();
        fakeService.setId(20L);

        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));

        when(reservationService.findReservationsByServiceAndDateForBranch(eq(10L), eq(20L), eq(fakeDate)))
                .thenReturn(List.of(buildReservationDTO(1L, StatusReservation.ACTIVE)));

        mockMvc.perform(get("/api/v1/admin/reservations/search")
                        .param("branchName", "Central Branch")
                        .param("serviceName", "Consultation")
                        .param("date", String.valueOf(fakeDate)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bankBranchService).findBranchByName("Central Branch");
        verify(bankServiceService).findServiceByName("Consultation");
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelReservation_ShouldReturnCancelledReservation_WhenReservationExists() throws Exception {
        when(reservationService.cancelReservation(1L))
                .thenReturn(buildReservationDTO(1L, StatusReservation.CANCELLED));

        mockMvc.perform(post("/api/v1/admin/reservations/{reservationId}/cancelReservation", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusReservation").value("CANCELLED"));

        verify(reservationService).cancelReservation(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelReservation_ShouldReturn404_WhenReservationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"))
                .when(reservationService).cancelReservation(99L);

        mockMvc.perform(post("/api/v1/admin/reservations/{reservationId}/cancelReservation", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void completeReservation_ShouldReturnCompletedReservation_WhenReservationExists() throws Exception {
        when(reservationService.completeReservation(1L))
                .thenReturn(buildReservationDTO(1L, StatusReservation.COMPLETED));

        mockMvc.perform(post("/api/v1/admin/reservations/{reservationId}/completeReservation", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusReservation").value("COMPLETED"));

        verify(reservationService).completeReservation(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void completeReservation_ShouldReturn404_WhenReservationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"))
                .when(reservationService).completeReservation(99L);

        mockMvc.perform(post("/api/v1/admin/reservations/{reservationId}/completeReservation", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReservation_ShouldReturnReservationDTO_WhenDataIsValid() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setBankBranchName("Central Branch");

        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);

        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));
        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(userService.findUserByUsername("alex")).thenReturn(fakeUser);
        when(reservationService.addReservation(any(), eq(1L), eq(fakeService), eq(fakeBranch)))
                .thenReturn(buildReservationDTO(1L, StatusReservation.ACTIVE));

        mockMvc.perform(post("/api/v1/admin/reservations")
                        .param("username", "alex")
                        .param("startReservation", LocalDateTime.now().plusDays(1).toString())
                        .param("serviceName", "Consultation")
                        .param("branchName", "Central Branch"))
                .andExpect(status().isCreated());

        verify(reservationService).addReservation(any(), eq(1L), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReservation_ShouldReturn409_WhenSlotIsTaken() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);

        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));
        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        when(userService.findUserByUsername("alex")).thenReturn(fakeUser);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "This time slot is already taken"))
                .when(reservationService).addReservation(any(), anyLong(), any(), any());

        mockMvc.perform(post("/api/v1/admin/reservations")
                        .param("username", "alex")
                        .param("startReservation", LocalDateTime.now().plusDays(1).toString())
                        .param("serviceName", "Consultation")
                        .param("branchName", "Central Branch"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReservation_ShouldReturn404_WhenUserNotFound() throws Exception {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();

        when(bankServiceService.findServiceByName("Consultation")).thenReturn(List.of(fakeService));
        when(bankBranchService.findBranchByName("Central Branch")).thenReturn(fakeBranch);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).findUserByUsername("ghost");

        mockMvc.perform(post("/api/v1/admin/reservations")
                        .param("username", "ghost")
                        .param("startReservation", LocalDateTime.now().plusDays(1).toString())
                        .param("serviceName", "Consultation")
                        .param("branchName", "Central Branch"))
                .andExpect(status().isNotFound());
    }

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
}
