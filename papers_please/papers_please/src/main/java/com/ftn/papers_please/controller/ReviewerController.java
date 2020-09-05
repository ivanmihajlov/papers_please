package com.ftn.papers_please.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.papers_please.mapper.PublishingProcessMapper;
import com.ftn.papers_please.mapper.ReviewRequestMapper;
import com.ftn.papers_please.security.TokenUtils;
import com.ftn.papers_please.service.CustomUserDetailsService;
import com.ftn.papers_please.service.PublishingProcessService;
import com.ftn.papers_please.dto.PublishingProcessDTO;
import com.ftn.papers_please.dto.ReviewRequestDTO;
import com.ftn.papers_please.model.scientific_paper.ScientificPaper;
import com.ftn.papers_please.model.publishing_process.PublishingProcess;
import com.ftn.papers_please.model.user.TRole;
import com.ftn.papers_please.dto.BasicUserInfoDTO;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.service.PaperService;

@RestController
@RequestMapping(value = "/api/reviewers")
@CrossOrigin()
public class ReviewerController {

	@Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private PublishingProcessService publishingProcessService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PublishingProcessMapper publishingProcessMapper;

    @Autowired
    private ReviewRequestMapper reviewRequestMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TokenUtils tokenUtils;
    
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ResponseEntity<List<BasicUserInfoDTO>> getAllReviewers() {
        String username = tokenUtils.getUsernameFromRequest(request);
        TUser user = userService.findByUsername(username);
        List<TUser> users = userService.findAll();
        List<BasicUserInfoDTO> response = new ArrayList<>();
        
        for (TUser u: users) {
            if (!u.getUserId().equals(user.getUserId())) {
                BasicUserInfoDTO dto = new BasicUserInfoDTO();
                dto.setUserId(u.getUserId());
                dto.setFullName(u.getName() + " " + u.getSurname());
                response.add(dto);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping(value="/html/paper/{processId}")
    public ResponseEntity<String> paperForReviewerHtml(@PathVariable("processId") String processId) throws Exception{
        PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
        if (process == null)
            return new ResponseEntity<>("Invalid process id", HttpStatus.NOT_FOUND);
        PublishingProcess.PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
        String paperId = latestVersion.getScientificPaperId();
		byte[] resource = paperService.anonymousFindOneHtml(paperId);
		return new ResponseEntity<>(new String(resource), HttpStatus.OK);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping(value="/pdf/paper/{processId}")
    public ResponseEntity<byte[]> paperForReviewerPdf(@PathVariable("processId") String processId) throws Exception{
        PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
        if (process == null)
            return new ResponseEntity("Invalid process id", HttpStatus.NOT_FOUND);
        PublishingProcess.PaperVersion latestVersion = process.getPaperVersion().get(process.getLatestVersion().intValue()-1);
        String paperId = latestVersion.getScientificPaperId();
		byte[] resource = paperService.anonymousFindOnePdf(paperId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add("Content-Disposition", "inline; filename=" + paperId + ".pdf");
		ResponseEntity<byte[]> response = new ResponseEntity<>(resource, headers, HttpStatus.OK);
		return response;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping(value ="/reviewRequests")
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    public ResponseEntity<ReviewRequestDTO> getPendingReviewRequests() {
        String username = tokenUtils.getUsernameFromRequest(request);
        TUser user = userService.findByUsername(username);
        List<ReviewRequestDTO> result = new ArrayList<>();
        List<PublishingProcess> processes = publishingProcessService.getReviewsByStatus(user.getUserId(), "PENDING");

        for (PublishingProcess process : processes) {
            if (!isInOngoingProcess(process))
                continue;
            ScientificPaper scientificPaper = paperService.findOneUnmarshalled(process.getPaperVersion().get(process.getLatestVersion().intValue()-1).getScientificPaperId());
            ReviewRequestDTO dto = reviewRequestMapper.toDTO(scientificPaper, process);
            result.add(dto);
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping(value ="/assignedReviews")
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    public ResponseEntity<ReviewRequestDTO> getAssignedReviews() {
        String username = tokenUtils.getUsernameFromRequest(request);
        TUser user = userService.findByUsername(username);
        List<ReviewRequestDTO> result = new ArrayList<>();
        List<PublishingProcess> processes = publishingProcessService.getReviewsByStatus(user.getUserId(), "ACCEPTED");

        for (PublishingProcess process : processes) {
            if (!isInOngoingProcess(process))
                continue;
            ScientificPaper scientificPaper = paperService.findOneUnmarshalled(process.getPaperVersion().get(process.getLatestVersion().intValue()-1).getScientificPaperId());
            ReviewRequestDTO dto = reviewRequestMapper.toDTO(scientificPaper, process);
            result.add(dto);
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@PutMapping(value = "/assign/{processId},{reviewerId}")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public ResponseEntity<PublishingProcessDTO> assignReviewer(@PathVariable("processId") String processId, @PathVariable("reviewerId") String reviewerId) {
        PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
        if (process == null)
            return new ResponseEntity("Invalid process ID", HttpStatus.NOT_FOUND);
        if (!process.getStatus().equals("NEW_SUBMISSION") && !process.getStatus().equals("NEW_REVIEWER_NEEDED") && !process.getStatus().equals("NEW_REVISION"))
            return new ResponseEntity("Cannot assign reviewer at this publishing process state", HttpStatus.BAD_REQUEST);

        TUser user = userService.findById(reviewerId);
        if (!checkIfReviewer(user.getRoles().getRole())) {
            user.getRoles().getRole().add(new TRole("ROLE_REVIEWER"));
            userService.update(user);
        }

        publishingProcessService.assignReviewer(process, user);

        ScientificPaper scientificPaper = paperService.findOneUnmarshalled(process.getPaperVersion().get(process.getLatestVersion().intValue()-1).getScientificPaperId());
        PublishingProcessDTO publishingProcessDTO = publishingProcessMapper.toDTO(scientificPaper, process, process.getLatestVersion().intValue()-1);
        return new ResponseEntity<>(publishingProcessDTO, HttpStatus.OK);
    }
    
    @SuppressWarnings("rawtypes")
	@PostMapping(value="/accept/{processId}")
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    public ResponseEntity acceptReview(@PathVariable("processId") String processId) {
        String username = tokenUtils.getUsernameFromRequest(request);
        TUser user = userService.findByUsername(username);
        PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
        if (process == null)
            return new ResponseEntity<>("Invalid process id", HttpStatus.NOT_FOUND);
        publishingProcessService.acceptOrRejectReview(process, user.getUserId(), true);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
	@SuppressWarnings("rawtypes")
    @PostMapping(value="/reject/{processId}")
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    public ResponseEntity rejectReview(@PathVariable("processId") String processId) {
		String username = tokenUtils.getUsernameFromRequest(request);
        TUser user = userService.findByUsername(username);
        PublishingProcess process = publishingProcessService.findOneUnmarshalled(processId);
        if (process == null)
            return new ResponseEntity<>("Invalid process id", HttpStatus.NOT_FOUND);
        publishingProcessService.acceptOrRejectReview(process, user.getUserId(), false);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    private boolean isInOngoingProcess(PublishingProcess process) {
        String status = process.getStatus();
        if (status.equals("ACCEPTED") || status.equals("REJECTED") || status.equals("WITHDRAWN"))
            return false;
        return true;
    }

    private boolean checkIfReviewer(List<TRole> roles) {
        for (TRole role : roles)
            if (role.getRole().equals("ROLE_REVIEWER"))
                return true;
        return false;
    }
    
}
