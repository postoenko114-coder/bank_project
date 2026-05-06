package com.example.demo.unit.service;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.dto.BankServiceDTO;
import com.example.demo.dto.LocationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.Location;
import com.example.demo.models.branch.WorkingHour;
import com.example.demo.repositories.BankBranchRepository;
import com.example.demo.repositories.BankServiceRepository;
import com.example.demo.services.bankBranch.BankBranchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankBranchServiceTest {

    @Mock
    private BankBranchRepository bankBranchRepository;

    @Mock
    private BankServiceRepository bankServiceRepository;

    @InjectMocks
    private BankBranchServiceImpl bankBranchServiceImpl;

    @Test
    void addBankBranch_ShouldUseProvidedCoordinates_WhenBothArePresent() {
        BankBranchDTO branchDTO = new BankBranchDTO();
        branchDTO.setBankBranchName("Central Branch");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setCity("London");
        locationDTO.setLatitude(51.5074);
        locationDTO.setLongitude(-0.1278);

        when(bankBranchRepository.save(any(BankBranch.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        bankBranchServiceImpl.addBankBranch(branchDTO, locationDTO);

        ArgumentCaptor<BankBranch> captor = ArgumentCaptor.forClass(BankBranch.class);

        verify(bankBranchRepository, times(1)).save(captor.capture());

        BankBranch savedBranch = captor.getValue();

        assertEquals(51.5074, savedBranch.getLocation().getLatitude());
        assertEquals(-0.1278, savedBranch.getLocation().getLongitude());
        assertEquals("Central Branch", savedBranch.getBankBranchName());
    }

    @Test
    void addBankBranch_ShouldEnrichCoordinates_WhenCoordinatesAreNull() {
        BankBranchDTO branchDTO = new BankBranchDTO();
        branchDTO.setBankBranchName("Hidden City");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setCity("London");

        when(bankBranchRepository.save(any(BankBranch.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        bankBranchServiceImpl.addBankBranch(branchDTO, locationDTO);

        ArgumentCaptor<BankBranch> captor = ArgumentCaptor.forClass(BankBranch.class);
        verify(bankBranchRepository, times(1)).save(captor.capture());

        BankBranch savedBranch = captor.getValue();

        assertNotNull(savedBranch.getLocation().getLatitude());
        assertNotNull(savedBranch.getLocation().getLongitude());
    }

    @Test
    void updateBankBranch_ShouldUseBankServiceRepository_WhenBankServicesAreNotNullAndBranchIsExistsAndNameChanged() {
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("FakeService");

        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setServices(new HashSet<>());
        fakeBranch.setSchedule(new HashSet<>());
        fakeBranch.setLocation(new Location());
        fakeBranch.setReservations(new ArrayList<>());

        BankServiceDTO fakeServiceDTO = new BankServiceDTO();
        fakeServiceDTO.setBankServiceName("FakeServiceDTO");

        BankBranchDTO fakeBranchDTO = new BankBranchDTO();
        fakeBranchDTO.setBankBranchName("FakeBranch");
        fakeBranchDTO.setBankServices(List.of(fakeServiceDTO));

        List<BankService> fakeBankServiceList = new ArrayList<>(List.of(fakeService));

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));
        when(bankServiceRepository.findAllById(any(List.class))).thenReturn(fakeBankServiceList);
        when(bankBranchRepository.save(any(BankBranch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bankBranchServiceImpl.updateBankBranch(1L, fakeBranchDTO);

        ArgumentCaptor<BankBranch> captor = ArgumentCaptor.forClass(BankBranch.class);
        verify(bankBranchRepository, times(1)).save(captor.capture());

        assertEquals(fakeBranchDTO.getBankBranchName(), captor.getValue().getBankBranchName());
        verify(bankServiceRepository, times(1)).findAllById(any(List.class));
    }

    @Test
    void updateBankBranch_ShouldThrowException_WhenBankBranchDoesntExists() {
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result = assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.updateBankBranch(1L, new BankBranchDTO()));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Bank Branch not found", result.getReason());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void getBranchById_ShouldReturnBankBranchDTO_WhenBranchExists() {
        BankBranch fakeBranch = new BankBranch();
        fakeBranch.setId(1L);
        fakeBranch.setServices(new HashSet<>());
        fakeBranch.setSchedule(new HashSet<>());
        fakeBranch.setLocation(new Location());
        fakeBranch.setReservations(new ArrayList<>());

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBranch));

        BankBranchDTO result = bankBranchServiceImpl.getBankBranchById(1L);

        assertNotNull(result);
        assertEquals(fakeBranch.getBankBranchName(), result.getBankBranchName());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void getBranchById_ShouldThrowException_WhenBankBranchDoesntExists() {
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result = assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.updateBankBranch(1L, new BankBranchDTO()));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Bank Branch not found", result.getReason());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void getAllBankBranches_ShouldReturnAllBankBranchDTO() {
        List<BankBranch> fakeBankBranchList = new ArrayList<>(List.of(new BankBranch(), new BankBranch()));

        when(bankBranchRepository.findAll()).thenReturn(fakeBankBranchList);

        List<BankBranchDTO> result = bankBranchServiceImpl.getAllBankBranches();

        assertNotNull(result);
        assertEquals(fakeBankBranchList.size(), result.size());
        verify(bankBranchRepository, times(1)).findAll();
    }

    @Test
    void getBranchesByService_ShouldReturnListDTOs_WhenServiceNameIsNotNull() {
        BankService fakeService = new BankService();
        fakeService.setBankServiceName("FakeService");

        BankBranch fakeBankBranch1 = new BankBranch();
        fakeBankBranch1.setId(1L);
        fakeBankBranch1.setSchedule(new HashSet<>());
        fakeBankBranch1.setLocation(new Location());
        fakeBankBranch1.setServices(Set.of(fakeService));

        BankBranch fakeBankBranch2 = new BankBranch();
        fakeBankBranch2.setId(2L);
        fakeBankBranch2.setSchedule(new HashSet<>());
        fakeBankBranch2.setLocation(new Location());
        fakeBankBranch2.setServices(Set.of(fakeService));

        List<BankBranch> fakeBankBranchList = new ArrayList<>(List.of(fakeBankBranch1, fakeBankBranch2));

        String fakeServiceName = "FakeService";

        when(bankBranchRepository.findByServiceName(fakeServiceName)).thenReturn(fakeBankBranchList);

        List<BankBranchDTO> result = bankBranchServiceImpl.getBranchesByService(fakeServiceName);

        assertNotNull(result);
        assertEquals(fakeBankBranchList.size(), result.size());
        verify(bankBranchRepository, times(1)).findByServiceName(fakeServiceName);
    }

    @Test
    void getBranchesByService_ShouldReturnListDTOs_WhenServiceNameIsNull() {
        String fakeString = null;

        ResponseStatusException result = assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.getBranchesByService(fakeString));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Service Name is null", result.getReason());
        verify(bankBranchRepository, times(0)).findByServiceName(fakeString);
    }

    @Test
    void getBranchesByLocation_ShouldReturnListDTOs_WhenCityAndStreetIsNotNull() {
        Location fakeLocation = new Location();
        fakeLocation.setCity("FakeCity");
        fakeLocation.setAddress("FakeStreet");

        String fakeCity = "FakeCity";
        String fakeStreet = "FakeStreet";

        BankBranch fakeBankBranch1 = new BankBranch();
        fakeBankBranch1.setId(1L);
        fakeBankBranch1.setSchedule(new HashSet<>());
        fakeBankBranch1.setLocation(fakeLocation);
        fakeBankBranch1.setServices(new HashSet<>());

        List<BankBranch> fakeBankBranchList = new ArrayList<>(List.of(fakeBankBranch1));

        when(bankBranchRepository.searchByCityAndStreetPartially(fakeCity, fakeStreet)).thenReturn(fakeBankBranchList);

        List<BankBranchDTO> result = bankBranchServiceImpl.getBranchesByLocation(fakeCity, fakeStreet);

        assertNotNull(result);
        assertEquals(fakeBankBranchList.size(), result.size());
        verify(bankBranchRepository, times(1)).searchByCityAndStreetPartially(fakeCity, fakeStreet);
    }

    @Test
    void  getBranchesByLocation_ShouldReturnListDTOs_WhenCityAndStreetIsNull() {
        String  fakeCity = null;
        String fakeStreet = null ;

        ResponseStatusException result = assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.getBranchesByLocation(fakeCity, fakeStreet));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("City and Street are both null", result.getReason());
    }

    @Test
    void getNearestBranches_ShouldThrowBadRequest_WhenCoordinatesAreMissing() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankBranchServiceImpl.getNearestBranches(null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("We dont have your coordinates", exception.getReason());

    }

    @Test
    void getNearestBranches_ShouldThrowBadRequest_WhenOneCoordinateIsMissing() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankBranchServiceImpl.getNearestBranches(50.45, null) // Передали только одну
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getNearestBranches_ShouldReturnListDTOs_WhenCoordinatesAreProvided() {
        Double lat = 50.45466;
        Double lot = 30.5238;

        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());
        fakeBankBranch.setServices(new HashSet<>());

        List<BankBranch> dbResponse = List.of(fakeBankBranch);

        when(bankBranchRepository.findNearestBranchesNative(lat, lot)).thenReturn(dbResponse);

        List<BankBranchDTO> result = bankBranchServiceImpl.getNearestBranches(lat, lot);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Central Branch", result.get(0).getBankBranchName());

        verify(bankBranchRepository, times(1)).findNearestBranchesNative(lat, lot);
    }

    @Test
    void addBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBranchAndServiceIsPresent() {
        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());

        BankService fakeBankService = new BankService();
        fakeBankService.setId(1L);
        fakeBankService.setReservations(new ArrayList<>());

        fakeBankBranch.setServices(new HashSet<>());
        fakeBankService.setBankBranches(new HashSet<>());

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeBankService));
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBankBranch));
        when(bankBranchRepository.save(fakeBankBranch)).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankServiceRepository.save(fakeBankService)).thenAnswer(invocation -> invocation.getArgument(0));

        bankBranchServiceImpl.addBankServiceToBranch(1L, 1L);

        ArgumentCaptor<BankBranch> captor1 = ArgumentCaptor.forClass(BankBranch.class);
        ArgumentCaptor<BankService> captor2 = ArgumentCaptor.forClass(BankService.class);

        verify(bankBranchRepository, times(1)).save(captor1.capture());
        verify(bankServiceRepository, times(1)).save(captor2.capture());

        BankBranch savedBranch = captor1.getValue();
        BankService savedService = captor2.getValue();

        assertEquals(fakeBankBranch.getBankServices().size(), savedBranch.getBankServices().size());
        assertEquals(fakeBankService.getBankBranches().size(), savedService.getBankBranches().size());

    }

    @Test
    void addBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBankServiceIsNotPresent() {
        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.addBankServiceToBranch(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Service not found", result.getReason());
    }

    @Test
    void addBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBankBranchIsNotPresent() {
        BankService fakeBankService = new BankService();
        fakeBankService.setId(1L);

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeBankService));

        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.addBankServiceToBranch(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Branch not found", result.getReason());
    }

    @Test
    void removeBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBranchAndServiceIsPresent() {
        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());

        BankService fakeBankService = new BankService();
        fakeBankService.setId(1L);
        fakeBankService.setReservations(new ArrayList<>());

        fakeBankBranch.setServices(new HashSet<>(Set.of(fakeBankService)));
        fakeBankService.setBankBranches(new HashSet<>(Set.of(fakeBankBranch)));

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeBankService));
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBankBranch));
        when(bankBranchRepository.save(fakeBankBranch)).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankServiceRepository.save(fakeBankService)).thenAnswer(invocation -> invocation.getArgument(0));

        bankBranchServiceImpl.deleteBankServiceFromBranch(1L, 1L);

        ArgumentCaptor<BankBranch> captor1 = ArgumentCaptor.forClass(BankBranch.class);
        ArgumentCaptor<BankService> captor2 = ArgumentCaptor.forClass(BankService.class);

        verify(bankBranchRepository, times(1)).save(captor1.capture());
        verify(bankServiceRepository, times(1)).save(captor2.capture());

        BankBranch savedBranch = captor1.getValue();
        BankService savedService = captor2.getValue();

        assertEquals(0, savedBranch.getBankServices().size());
        assertEquals(0, savedService.getBankBranches().size());

    }

    @Test
    void deleteBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBankServiceIsNotPresent() {
        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.deleteBankServiceFromBranch(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Service not found", result.getReason());
    }

    @Test
    void deleteBankServiceToBranch_ShouldReturnBankBranchDTO_WhenBankBranchIsNotPresent() {
        BankService fakeBankService = new BankService();
        fakeBankService.setId(1L);

        when(bankServiceRepository.findById(1L)).thenReturn(Optional.of(fakeBankService));

        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.deleteBankServiceFromBranch(1L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Branch not found", result.getReason());
    }

    @Test
    void getServicesOfBranch_ShouldReturnListServiceDTOs_WhenBankBranchIsPresent() {
        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());

        BankService fakeBankService = new BankService();
        fakeBankService.setId(1L);
        fakeBankService.setBankBranches(new HashSet<>(Set.of(fakeBankBranch)));
        fakeBankService.setReservations(new ArrayList<>());

        fakeBankBranch.setServices(Set.of(fakeBankService));

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBankBranch));

        List<BankServiceDTO> fakeBankServiceDTOs = bankBranchServiceImpl.getBankServicesOfBranch(1L);

        assertNotNull(fakeBankServiceDTOs);
        assertEquals(fakeBankServiceDTOs.size(), fakeBankService.getBankBranches().size());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void getServicesOfBranch_ShouldThrowsException_WhenBankBranchIsNotPresent() {
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.getBankServicesOfBranch(1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Branch not found", result.getReason());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void isBranchOpen_ShouldReturnTrue_WhenBankBranchIsOpenAndExists() {
        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(DayOfWeek.MONDAY);
        workingHour.setOpenTime(LocalTime.of(1,0,0));
        workingHour.setCloseTime(LocalTime.of(23,0,0));

        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(Set.of(workingHour));
        fakeBankBranch.setLocation(new Location());
        fakeBankBranch.setServices(Set.of(new BankService()));

        LocalDateTime fakeTime = LocalDateTime.of(2027,12,6,10,1);

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBankBranch));

        Boolean result = bankBranchServiceImpl.isBranchOpen(1L, fakeTime);

        assertTrue(result);
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void isBranchOpen_ShouldReturnFalse_WhenBankBranchIsOpenAndExists() {
        WorkingHour workingHour = new WorkingHour();
        workingHour.setDayOfWeek(DayOfWeek.MONDAY);
        workingHour.setOpenTime(LocalTime.of(1,0,0));
        workingHour.setCloseTime(LocalTime.of(23,0,0));

        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(Set.of(workingHour));
        fakeBankBranch.setLocation(new Location());
        fakeBankBranch.setServices(Set.of(new BankService()));

        LocalDateTime fakeTime = LocalDateTime.of(2027,12,7,10,1);

        when(bankBranchRepository.findById(1L)).thenReturn(Optional.of(fakeBankBranch));

        Boolean result = bankBranchServiceImpl.isBranchOpen(1L, fakeTime);

        assertFalse(result);
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void isisBranchOpen_ShouldThrowsException_WhenBankBranchDoesntExists(){
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.isBranchOpen(1L, LocalDateTime.now()));
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Bank Branch not found", result.getReason());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void findBranchByName_ShouldReturnBankBranch_WhenBankBranchExists() {
        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());
        fakeBankBranch.setServices(Set.of(new BankService()));

        when(bankBranchRepository.findByName(fakeBankBranch.getBankBranchName())).thenReturn(Optional.of(fakeBankBranch));

        BankBranch result = bankBranchServiceImpl.findBranchByName(fakeBankBranch.getBankBranchName());

        assertNotNull(result);
        assertEquals(fakeBankBranch.getBankBranchName(), result.getBankBranchName());
        assertEquals(fakeBankBranch.getId(), result.getId());
        verify(bankBranchRepository, times(1)).findByName(fakeBankBranch.getBankBranchName());
    }


    @Test
    void findBranchByName_ShouldThrowsException_WhenBankBranchDoesntExists(){
        when(bankBranchRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result =  assertThrows(ResponseStatusException.class, () -> bankBranchServiceImpl.isBranchOpen(1L, LocalDateTime.now()));
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Bank Branch not found", result.getReason());
        verify(bankBranchRepository, times(1)).findById(1L);
    }

    @Test
    void deleteBankBranch_ShouldBeDeleted(){
        BankBranch fakeBankBranch = new BankBranch();
        fakeBankBranch.setId(1L);
        fakeBankBranch.setBankBranchName("Central Branch");
        fakeBankBranch.setSchedule(new HashSet<>());
        fakeBankBranch.setLocation(new Location());
        fakeBankBranch.setServices(Set.of(new BankService()));

        bankBranchServiceImpl.deleteBankBranch(fakeBankBranch.getId());
        verify(bankBranchRepository, times(1)).deleteById(fakeBankBranch.getId());
    }

}
