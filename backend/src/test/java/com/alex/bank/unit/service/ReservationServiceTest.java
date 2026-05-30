package com.alex.bank.unit.service;

import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.mapper.ReservationMapper;
import com.alex.bank.mapper.ReservationMapperImpl;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.models.branch.reservation.Reservation;
import com.alex.bank.models.branch.reservation.StatusReservation;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.ReservationRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.reservation.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankBranchService bankBranchService;

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

    private ReservationMapper reservationMapper = new ReservationMapperImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationServiceImpl, "reservationMapper", reservationMapper);
    }

    @Test
    void addReservation_ShouldReturnReservationDTO_WhenAllConditionsAreMet() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        LocalDateTime startTime = LocalDateTime.of(2026, Month.OCTOBER, 12, 12, 59, 59);

        when(bankBranchService.isBranchOpen(eq(fakeBranch.getId()), any(LocalDateTime.class))).thenReturn(true);
        when(reservationRepository.existsOverlappingReservation(eq(fakeService.getId()), eq(startTime), any(LocalDateTime.class))).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationDTO result = reservationServiceImpl.addReservation(startTime, 1L, fakeService, fakeBranch);

        assertNotNull(result);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());

        Reservation savedReservation = captor.getValue();

        assertEquals(StatusReservation.ACTIVE, savedReservation.getStatus());
        assertEquals(fakeUser, savedReservation.getUser());
        assertEquals(startTime, savedReservation.getStartReservation());
        assertNotNull(savedReservation.getEndReservation());
    }

    @Test
    void addReservation_ShouldThrowBadRequest_WhenStartTimeIsInThePast() {
        BankService fakeService = new BankService();
        BankBranch fakeBranch = new BankBranch();

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.addReservation(pastTime, 1L, fakeService, fakeBranch));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cannot book in the past", exception.getReason());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservation_ShouldThrowBadRequest_WhenBranchIsNotOpenAtStartTime() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        when(bankBranchService.isBranchOpen(1L, startTime)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.addReservation(startTime, 1L, fakeService, fakeBranch));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Branch is not open in this time", exception.getReason());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservation_ShouldThrowConflict_WhenTimeSlotIsAlreadyTaken() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        when(bankBranchService.isBranchOpen(eq(1L), any(LocalDateTime.class))).thenReturn(true);
        when(reservationRepository.existsOverlappingReservation(eq(1L), eq(startTime), any(LocalDateTime.class))).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.addReservation(startTime, 1L, fakeService, fakeBranch));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("This time slot is already taken", exception.getReason());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void addReservation_ShouldThrowNotFound_WhenUserDoesNotExist() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setDuration("30 min");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        when(bankBranchService.isBranchOpen(eq(1L), any(LocalDateTime.class))).thenReturn(true);
        when(reservationRepository.existsOverlappingReservation(eq(1L), eq(startTime), any(LocalDateTime.class))).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.addReservation(startTime, 1L, fakeService, fakeBranch));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getReservationById_ShouldReturnReservationDTO_WhenReservationExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setBankBranchName("Central");

        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");

        Reservation fakeReservation = new Reservation();
        fakeReservation.setId(1L);
        fakeReservation.setStatus(StatusReservation.ACTIVE);
        fakeReservation.setUser(fakeUser);
        fakeReservation.setBankBranch(fakeBranch);
        fakeReservation.setBankService(fakeService);
        fakeReservation.setStartReservation(LocalDateTime.now().plusDays(1));
        fakeReservation.setEndReservation(LocalDateTime.now().plusDays(1).plusMinutes(30));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(fakeReservation));

        ReservationDTO result = reservationServiceImpl.getReservationById(1L);

        assertNotNull(result);
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void getReservationById_ShouldThrowNotFound_WhenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.getReservationById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Reservation not found", exception.getReason());
    }

    @Test
    void getAllReservations_ShouldReturnReservationDTOList() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setBankBranchName("Branch1");

        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Service1");

        Reservation r1 = new Reservation();
        r1.setStatus(StatusReservation.ACTIVE);
        r1.setUser(fakeUser);
        r1.setBankBranch(fakeBranch);
        r1.setBankService(fakeService);
        r1.setStartReservation(LocalDateTime.now().plusDays(1));
        r1.setEndReservation(LocalDateTime.now().plusDays(1).plusMinutes(30));

        Reservation r2 = new Reservation();
        r2.setStatus(StatusReservation.COMPLETED);
        r2.setUser(fakeUser);
        r2.setBankBranch(fakeBranch);
        r2.setBankService(fakeService);
        r2.setStartReservation(LocalDateTime.now().plusDays(2));
        r2.setEndReservation(LocalDateTime.now().plusDays(2).plusMinutes(30));

        when(reservationRepository.findAll()).thenReturn(List.of(r1, r2));

        List<ReservationDTO> result = reservationServiceImpl.getAllReservations();

        assertEquals(2, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getAllReservationsOfUser_ShouldReturnReservationDTOList_WhenUserExists() {
        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setBankBranchName("Branch1");

        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Service1");

        Reservation r1 = new Reservation();
        r1.setStatus(StatusReservation.ACTIVE);
        r1.setBankBranch(fakeBranch);
        r1.setBankService(fakeService);
        r1.setStartReservation(LocalDateTime.now().plusDays(1));
        r1.setEndReservation(LocalDateTime.now().plusDays(1).plusMinutes(30));

        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setReservations(List.of(r1));

        r1.setUser(fakeUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

        List<ReservationDTO> result = reservationServiceImpl.getAllReservationsOfUser(1L);

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllReservationsOfUser_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.getAllReservationsOfUser(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void cancelReservation_ShouldSetStatusToCancelled_WhenReservationExists() {
        Reservation fakeReservation = new Reservation();
        fakeReservation.setId(1L);
        fakeReservation.setStatus(StatusReservation.ACTIVE);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setBankBranchName("Branch1");
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Service1");
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");

        fakeReservation.setBankBranch(fakeBranch);
        fakeReservation.setBankService(fakeService);
        fakeReservation.setUser(fakeUser);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(fakeReservation));

        reservationServiceImpl.cancelReservation(1L);

        assertEquals(StatusReservation.CANCELLED, fakeReservation.getStatus());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void cancelReservation_ShouldThrowNotFound_WhenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.cancelReservation(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Reservation not found", exception.getReason());
    }

    @Test
    void completeReservation_ShouldReturnReservationDTOWithStatusCompleted_WhenReservationExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setBankBranchName("Branch1");

        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Service1");

        Reservation fakeReservation = new Reservation();
        fakeReservation.setId(1L);
        fakeReservation.setStatus(StatusReservation.ACTIVE);
        fakeReservation.setUser(fakeUser);
        fakeReservation.setBankBranch(fakeBranch);
        fakeReservation.setBankService(fakeService);
        fakeReservation.setStartReservation(LocalDateTime.now().plusDays(1));
        fakeReservation.setEndReservation(LocalDateTime.now().plusDays(1).plusMinutes(30));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(fakeReservation));

        ReservationDTO result = reservationServiceImpl.completeReservation(1L);

        assertNotNull(result);
        assertEquals(StatusReservation.COMPLETED, fakeReservation.getStatus());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void completeReservation_ShouldThrowNotFound_WhenReservationDoesNotExist() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationServiceImpl.completeReservation(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Reservation not found", exception.getReason());
    }

    @Test
    void findReservationsByServiceAndDateForBranch_ShouldReturnFilteredReservationDTOs() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setBankBranchName("Branch1");

        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Service1");

        LocalDate date = LocalDate.now().plusDays(1);

        Reservation r1 = new Reservation();
        r1.setStatus(StatusReservation.ACTIVE);
        r1.setUser(fakeUser);
        r1.setBankBranch(fakeBranch);
        r1.setBankService(fakeService);
        r1.setStartReservation(date.atTime(10, 0));
        r1.setEndReservation(date.atTime(10, 30));

        when(reservationRepository.findReservationsByFilters(eq(1L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(r1));

        List<ReservationDTO> result = reservationServiceImpl.findReservationsByServiceAndDateForBranch(1L, 1L, date);

        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).findReservationsByFilters(eq(1L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

}
