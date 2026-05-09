package com.example.demo.unit.service;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.WorkingHour;
import com.example.demo.repositories.BankBranchRepository;
import com.example.demo.repositories.BankServiceRepository;
import com.example.demo.repositories.ReservationRepository;
import com.example.demo.services.bankService.BankServiceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankServiceServiceTest {

    @Mock
    private BankServiceRepository bankServiceRepository;

    @Mock
    private BankBranchRepository bankBranchRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private BankServiceServiceImpl bankServiceServiceImpl;

    // ───── addService ─────

    @Test
    void addService_ShouldReturnBankServiceDTO_WhenDataIsCorrect() {
        BankServiceDTO fakeDTO = new BankServiceDTO();
        fakeDTO.setBankServiceName("Loan Consultation");
        fakeDTO.setDuration("30 min");
        fakeDTO.setDescription("Consulting about loans");

        when(bankServiceRepository.save(any(BankService.class))).thenAnswer(inv -> inv.getArgument(0));

        BankServiceDTO result = bankServiceServiceImpl.addService(fakeDTO);

        assertNotNull(result);
        assertEquals("Loan Consultation", result.getBankServiceName());
        assertEquals("30 min", result.getDuration());
        verify(bankServiceRepository, times(1)).save(any(BankService.class));
    }

    // ───── getServiceById ─────

    @Test
    void getServiceById_ShouldReturnBankServiceDTO_WhenServiceExists() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Account Opening");
        fakeService.setDuration("15 min");
        fakeService.setDescription("Opening a new account");

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));

        BankServiceDTO result = bankServiceServiceImpl.getServiceById(1L);

        assertNotNull(result);
        assertEquals("Account Opening", result.getBankServiceName());
        verify(bankServiceRepository, times(1)).findById(1L);
    }

    @Test
    void getServiceById_ShouldThrowNotFound_WhenServiceDoesNotExist() {
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankServiceServiceImpl.getServiceById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Bank Service Not Found", exception.getReason());
    }

    // ───── getServicesList ─────

    @Test
    void getServicesList_ShouldReturnAllServicesAsDTOs() {
        BankService service1 = new BankService();
        service1.setBankServiceName("Loan");
        service1.setDuration("30 min");
        service1.setDescription("Loan consultation");

        BankService service2 = new BankService();
        service2.setBankServiceName("Card Issue");
        service2.setDuration("15 min");
        service2.setDescription("Issuing new card");

        when(bankServiceRepository.findAll()).thenReturn(List.of(service1, service2));

        List<BankServiceDTO> result = bankServiceServiceImpl.getServicesList();

        assertEquals(2, result.size());
        assertEquals("Loan", result.get(0).getBankServiceName());
        assertEquals("Card Issue", result.get(1).getBankServiceName());
        verify(bankServiceRepository, times(1)).findAll();
    }

    // ───── findServiceByName ─────

    @Test
    void findServiceByName_ShouldReturnBankServiceList_WhenServicesExist() {
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("Loan");

        when(bankServiceRepository.findByBankServiceName("Loan")).thenReturn(List.of(fakeService));

        List<BankService> result = bankServiceServiceImpl.findServiceByName("Loan");

        assertEquals(1, result.size());
        assertEquals("Loan", result.get(0).getBankServiceName());
        verify(bankServiceRepository, times(1)).findByBankServiceName("Loan");
    }

    // ───── updateService ─────

    @Test
    void updateService_ShouldReturnUpdatedDTO_WhenServiceExistsAndDataChanged() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Old Name");
        fakeService.setDuration("15 min");
        fakeService.setDescription("Old description");

        BankServiceDTO fakeDTO = new BankServiceDTO();
        fakeDTO.setBankServiceName("New Name");
        fakeDTO.setDuration("30 min");
        fakeDTO.setDescription("New description");

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));

        BankServiceDTO result = bankServiceServiceImpl.updateService(1L, fakeDTO);

        assertNotNull(result);
        assertEquals("New Name", result.getBankServiceName());
        assertEquals("30 min", result.getDuration());
        assertEquals("New description", result.getDescription());
        assertEquals("New Name", fakeService.getBankServiceName());
        verify(bankServiceRepository, times(1)).findById(1L);
    }

    @Test
    void updateService_ShouldReturnSameDTO_WhenNothingChanged() {
        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Same Name");
        fakeService.setDuration("30 min");
        fakeService.setDescription("Same description");

        BankServiceDTO fakeDTO = new BankServiceDTO();
        fakeDTO.setBankServiceName("Same Name");
        fakeDTO.setDuration("30 min");
        fakeDTO.setDescription("Same description");

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));

        BankServiceDTO result = bankServiceServiceImpl.updateService(1L, fakeDTO);

        assertNotNull(result);
        assertEquals("Same Name", result.getBankServiceName());
        verify(bankServiceRepository, times(1)).findById(1L);
    }

    @Test
    void updateService_ShouldThrowNotFound_WhenServiceDoesNotExist() {
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankServiceServiceImpl.updateService(1L, new BankServiceDTO()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Bank Service Not Found", exception.getReason());
    }

    // ───── deleteService ─────

    @Test
    void deleteService_ShouldCallRepositoryDelete() {
        bankServiceServiceImpl.deleteService(1L);

        verify(bankServiceRepository, times(1)).deleteById(1L);
    }

    // ───── getAvailabilityServiceByDate ─────

    @Test
    void getAvailabilityServiceByDate_ShouldReturnTrue_WhenBranchIsOpenAndSlotsAvailable() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(dayOfWeek);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setSchedule(Set.of(workingHour));

        BankService fakeService = new BankService();
        fakeService.setId(1L);
        fakeService.setBankServiceName("Consultation");

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));
        when(reservationRepository.countBookedSlots(eq(1L), eq(1L), any(), any())).thenReturn(3L);

        Boolean result = bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, date);

        assertTrue(result);
        verify(reservationRepository, times(1)).countBookedSlots(eq(1L), eq(1L), any(), any());
    }

    @Test
    void getAvailabilityServiceByDate_ShouldReturnFalse_WhenAllSlotsAreTaken() {
        LocalDate date = LocalDate.now().plusDays(1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(dayOfWeek);

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setSchedule(Set.of(workingHour));

        BankService fakeService = new BankService();
        fakeService.setId(1L);

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));
        when(reservationRepository.countBookedSlots(eq(1L), eq(1L), any(), any())).thenReturn(10L);

        Boolean result = bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, date);

        assertFalse(result);
    }

    @Test
    void getAvailabilityServiceByDate_ShouldReturnFalse_WhenBranchIsClosedOnThatDay() {
        LocalDate date = LocalDate.of(2025, 1, 6); // Monday

        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(DayOfWeek.SATURDAY); // branch only open on Saturday

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setSchedule(Set.of(workingHour));

        BankService fakeService = new BankService();
        fakeService.setId(1L);

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeService));

        Boolean result = bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, date);

        assertFalse(result);
        verify(reservationRepository, never()).countBookedSlots(any(), any(), any(), any());
    }

    @Test
    void getAvailabilityServiceByDate_ShouldThrowBadRequest_WhenDateIsNull() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Date is required", exception.getReason());
    }

    @Test
    void getAvailabilityServiceByDate_ShouldThrowNotFound_WhenBranchDoesNotExist() {
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, LocalDate.now().plusDays(1)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Bank Branch Not Found", exception.getReason());
    }

    @Test
    void getAvailabilityServiceByDate_ShouldThrowNotFound_WhenServiceDoesNotExist() {
        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(DayOfWeek.MONDAY);
        workingHour.setOpenTime(LocalTime.of(1, 1));
        workingHour.setCloseTime(LocalTime.of(23, 23));

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setSchedule(Set.of(workingHour));

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));
        when(bankServiceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankServiceServiceImpl.getAvailabilityServiceByDate(1L, 1L, LocalDate.now().plusDays(1)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Bank Service Not Found", exception.getReason());
    }

}
