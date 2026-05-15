package com.example.demo.services.supportMessage;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.mapper.SupportMapper;
import com.example.demo.models.supportMessage.StatusSupportMessage;
import com.example.demo.models.supportMessage.SupportMessage;
import com.example.demo.repositories.SupportMessageRepository;
import com.example.demo.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SupportMessageServiceImpl implements SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;

    private final UserService userService;

    private final SupportMapper  supportMapper;

    @Transactional
    @Override
    public SupportDTO createSupportMessage(SupportDTO supportDTO, java.security.Principal principal) {
        String targetEmail;
        if (principal != null) {
            targetEmail = principal.getName();
        } else {
            targetEmail = supportDTO.getUserEmail();
            if (targetEmail == null || targetEmail.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to input email");
            }
        }

        SupportMessage supportMessage = supportMapper.toEntity(supportDTO);

        supportMessage.setUserEmail(targetEmail);

        supportMessageRepository.save(supportMessage);
        return supportMapper.toDTO(supportMessage);
    }

    @Transactional
    @Override
    public SupportDTO getSupportMessageById(Long supportMessage_id){
        SupportMessage message = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        return supportMapper.toDTO(message);
    }

    @Transactional
    @Override
    public List<SupportDTO> getAllSupportMessages(){
        List<SupportMessage> supportMessages = supportMessageRepository.findAll();
        List<SupportDTO> supportDTOs = new ArrayList<>();
        for (SupportMessage supportMessage : supportMessages) {
            supportDTOs.add(supportMapper.toDTO(supportMessage));
        }
        return supportDTOs;
    }

    @Transactional
    @Override
    public List<SupportDTO> search(String userEmail, LocalDate date){
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.atTime(LocalTime.MAX);
        }

        List<SupportMessage> supportMessages = supportMessageRepository.search(userEmail, startOfDay, endOfDay);
        List<SupportDTO> supportDTOs = new ArrayList<>();
        for(SupportMessage supportMessage : supportMessages){
            supportDTOs.add(supportMapper.toDTO(supportMessage));
        }
        return supportDTOs;
    }

    @Transactional
    @Override
    public void markAsInProgress(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.IN_PROGRESS);
        supportMessageRepository.save(supportMessage);
    }

    @Transactional
    @Override
    public void markAsCompleted(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.COMPLETED);
        supportMessageRepository.save(supportMessage);
    }
}
