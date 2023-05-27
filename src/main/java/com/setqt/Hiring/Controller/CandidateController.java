package com.setqt.Hiring.Controller;

import com.setqt.Hiring.DTO.*;
import com.setqt.Hiring.Model.*;
import com.setqt.Hiring.Security.JwtTokenHelper;
import com.setqt.Hiring.Security.Model.User;
import com.setqt.Hiring.Service.CV.CVService;
import com.setqt.Hiring.Service.Candidate.CandidateService;
import com.setqt.Hiring.Service.Company.CompanyService;
import com.setqt.Hiring.Service.Firebase.FirebaseDocumentFileService;
import com.setqt.Hiring.Service.Firebase.FirebaseImageService;
import com.setqt.Hiring.Service.JobPosting.JobPostingService;
import com.setqt.Hiring.Service.RatingCompany.RatingCompanyService;
import com.setqt.Hiring.Service.Report.ReportService;
import com.setqt.Hiring.Service.SavedJobPosting.SavedJobPostingService;
import com.setqt.Hiring.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/candidate")
public class CandidateController {
    @Autowired
    JwtTokenHelper jwtHelper;

    @Autowired
    JobPostingService jobPostingService;
    @Autowired
    ReportService reportService;
    @Autowired
    UserService uService;
    @Autowired
    private FirebaseImageService firebaseImageService;
    @Autowired
    private FirebaseDocumentFileService firebaseDocumentFileService;
    @Autowired
    CandidateService candidateService;
    @Autowired
    CompanyService companyService;

    @Autowired
    RatingCompanyService ratingCompanyService;

    @Autowired
    SavedJobPostingService savedJobPostingService;
    @Autowired
    CVService cvService;

    @GetMapping("/getAll")
    public ResponseEntity<ResponseObject> getAllCandidate(@RequestHeader(value = "Authorization") String jwt) {
        try {
            List<Candidate> result = candidateService.findAll();
            System.out.println(result.size());
            if (result.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "not found data", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "found data", result));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/myInfo")
    public ResponseEntity<ResponseObject> getMyInfo(@RequestHeader(value = "Authorization") String jwt) {
        try {
            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();
            if (candidate == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "not found data", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "found data", candidate));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PutMapping(value = "/updateInfoCandidate", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseObject> addCandidate(@RequestPart("candidate") CandidateDTO candidateDTO,
                                                       @RequestPart("file") MultipartFile file,
                                                       @RequestHeader(value = "Authorization") String jwt) {
        try {
            System.out.println(candidateDTO.toString());
            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();


            // xu li file
            firebaseImageService = new FirebaseImageService();
            // save file to Firebase
            String fileName = firebaseImageService.save(file, "avatars_candidate/" + candidate.getId() + "_" + candidate.getEmail());
            String imageUrl = firebaseImageService.getFileUrl(fileName);

            System.out.println((imageUrl));

            candidate.setFullName(candidateDTO.getFullname());
            candidate.setGender(candidateDTO.getGender());
            candidate.setPhone(candidateDTO.getPhone());
            candidate.setAddress(candidateDTO.getAddress());
            candidate.setAvatar(imageUrl);
            candidate.setExperience(candidateDTO.getExperience());
            candidate.setSkill(candidateDTO.getSkill());
            candidate.setDob(candidateDTO.getDob());

            Candidate result = candidateService.save(candidate);

            if (result == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "update info candidate failed", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "found data", result));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/addReport/{idPosting}")
    public ResponseEntity<ResponseObject> addReport(@PathVariable String idPosting,
                                                    @RequestBody ReportDTO reportDTO,
                                                    @RequestHeader(value = "Authorization") String jwt) {
        try {

            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();

            Optional<JobPosting> jobPosting = jobPostingService.findById(Long.parseLong(idPosting));
            Report report = new Report(reportDTO.getContent(), jobPosting.get(), candidate);

            Report result = reportService.save(report);

            if (result == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "add Report failed", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "add Report successfully", result));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/rating/{idCompany}")
    public ResponseEntity<ResponseObject> addReport(@PathVariable String idCompany,
                                                    @RequestBody RatingDTO ratingDTO,
                                                    @RequestHeader(value = "Authorization") String jwt) {
        try {

            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();

            Optional<Company> company = companyService.findById(Long.parseLong(idCompany));

            List<RatingCompany> ratingCompanyList = ratingCompanyService.findAll();

            // check exists rating
            for (RatingCompany a : ratingCompanyList) {
                if (Objects.equals(a.getCandidate().getId(), candidate.getId()) && Objects.equals(a.getCompany().getId(), company.get().getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ResponseObject("failed", "candidate has been rating", null));
                }
            }


            RatingCompany ratingCompany = new RatingCompany(ratingDTO.getRate(), ratingDTO.getContent(), company.get(), candidate);
            RatingCompany result = ratingCompanyService.save(ratingCompany);
            // update rating
            company.get().updateRating();
            companyService.save(company.get());

            if (result == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "add Rating failed", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "add Rating successfully", result));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/saveJobPosting/{idPosting}")
    public ResponseEntity<ResponseObject> saveJobPosting(@PathVariable String idPosting,
                                                         @RequestHeader(value = "Authorization") String jwt) {
        try {

            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();

            List<SavedJobPosting> savedJobPostingList = savedJobPostingService.findAll();

            for (SavedJobPosting savedJobPosting : savedJobPostingList) {
                if (savedJobPosting.getIdJobPosting() == Long.parseLong(idPosting) && Objects.equals(savedJobPosting.getCandidate().getId(), candidate.getId())) {
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "This job posting has been saved", null));
                }
            }

            Optional<JobPosting> jobPosting = jobPostingService.findById(Long.parseLong(idPosting));
            SavedJobPosting savedJobPosting = new SavedJobPosting(candidate, jobPosting.get());
            SavedJobPosting result = savedJobPostingService.save(savedJobPosting);

            if (result == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "save Job posting failed", null));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "save job posting successfully", result));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/deleteJobPosting/{idSaved}")
    public ResponseEntity<ResponseObject> deleteJobPosting(@PathVariable String idSaved,
                                                           @RequestHeader(value = "Authorization") String jwt) {
        try {

            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();


            savedJobPostingService.delete(Long.parseLong(idSaved));

            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "delete job posting successfully", null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("failed", "delete Job posting failed", null));
        }
    }

    @PostMapping(value = "/submitCV/{idPosting}", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseObject> submitCV(@PathVariable String idPosting,
                                                   @RequestPart("info") SubmitCVDTO submitCVDTO,
                                                   @RequestPart("file") MultipartFile file,
                                                   @RequestHeader(value = "Authorization") String jwt) {
        try {

            jwt = jwt.substring(7, jwt.length());

            String username = jwtHelper.getUsernameFromToken(jwt);
            System.out.println(username);
            User user = (User) uService.findOneByUsername(username);
            Candidate candidate = user.getCandidate();
            Optional<JobPosting> jobPosting = jobPostingService.findById(Long.parseLong(idPosting));

            // xu li file
            firebaseDocumentFileService = new FirebaseDocumentFileService();
            // save file to Firebase
            String fileName = firebaseDocumentFileService.save(file, candidate.getId() + "_" + submitCVDTO.getName() + "_" + idPosting);
            String url = firebaseDocumentFileService.getFileUrl(fileName);

            System.out.println((url));

            CV cv = new CV();
            cv.setCandidate(candidate);
            cv.setName(submitCVDTO.getName());
            cv.setIntroLetter(submitCVDTO.getIntroLetter());
            cv.setFileCV(url);
            cv.setDateCreated(new Date());
            cv.setJobPosting(jobPosting.get());

            CV result = cvService.save(cv);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject("failed", "submit for Job posting failed", null));

            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "submit CV for job posting successfully", result));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject("failed", "submit for Job posting failed", null));
        }
    }

}