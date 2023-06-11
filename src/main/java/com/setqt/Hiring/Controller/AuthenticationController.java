package com.setqt.Hiring.Controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import com.setqt.Hiring.DTO.EmployeeAuthedDTO;
import com.setqt.Hiring.Model.*;
import com.setqt.Hiring.Security.JwtTokenHelper;
import com.setqt.Hiring.Service.EmailService.EmailService;
import com.setqt.Hiring.Threads.EmailThreads;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
//
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;


import com.setqt.Hiring.DTO.CandidateAuthedDTO;
import com.setqt.Hiring.Security.Model.Role;
import com.setqt.Hiring.Security.Model.RoleRepository;
import com.setqt.Hiring.Security.Model.User;
import com.setqt.Hiring.Service.UserService;
import com.setqt.Hiring.Service.Candidate.CandidateService;
import com.setqt.Hiring.Service.Company.CompanyService;
import com.setqt.Hiring.Service.Employer.EmployerService;

import ch.qos.logback.classic.Logger;
import client.AuthenRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = {"Content-Type", "Authorization"})
public class AuthenticationController {

    @Autowired
    private RoleRepository roleRepo;
    @Autowired
    private UserService UService;
    @Autowired
    private CompanyService comService;
    @Autowired
    private EmployerService emService;
    @Autowired
    private PasswordEncoder passEncoder;
    Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    JwtTokenHelper jWTTokenHelper;

    @Autowired
    private Environment environment;
    @Autowired
    private EmailService emailService;


    @PostMapping("/loginCandidate")
    public ResponseEntity<?> loginCandidate(@RequestBody AuthenRequest authentRequest)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        try {
            final Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authentRequest.getUsername(), authentRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jWTTokenHelper.generateToken(authentRequest.getUsername());

            User user = (User) UService.findOneByUsername(authentRequest.getUsername());
            Set<Role> roles = new HashSet<>();
            roles = (Set<Role>) user.getRoles();
            boolean check = false;
            Iterator<Role> iterator = roles.iterator();
            while (iterator.hasNext()) {
                Role role = iterator.next();
                if(Objects.equals(role.getNameRole(), "CANDIDATE")){
                    check = true;
                    break;
                }
            }

            if(!check){
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("failed", "Đăng nhập không thành công",
                                ""));
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Đăng nhập thành công",
                            jwt));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("failed", "Đăng nhập không thành công",
                            ""));
        }

    }

    @PostMapping("/loginEmployer")
    public ResponseEntity<?> loginEmployer(@RequestBody AuthenRequest authentRequest)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        try {
            final Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authentRequest.getUsername(), authentRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jWTTokenHelper.generateToken(authentRequest.getUsername());

            User user = (User) UService.findOneByUsername(authentRequest.getUsername());
            Set<Role> roles = new HashSet<>();
            roles = (Set<Role>) user.getRoles();
            boolean check = false;
            Iterator<Role> iterator = roles.iterator();
            while (iterator.hasNext()) {
                Role role = iterator.next();
                if(Objects.equals(role.getNameRole(), "EMPLOYER")){
                    check = true;
                    break;
                }
            }

            if(!check){
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("failed", "Đăng nhập không thành công",
                                ""));
            }

//            if(!user.isEnable()){
//                return ResponseEntity.status(HttpStatus.OK).body(
//                        new ResponseObject("failed", "Bạn chưa xác thực tài khoản!",
//                                ""));
//            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Đăng nhập thành công",
                            jwt));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("failed", "Đăng nhập không thành công",
                            ""));
        }

    }

  
    @PostMapping(value = "/signup/candidate", consumes = {"application/json"})
    public ResponseEntity<ResponseObject> createAccountCDD(@RequestBody CandidateAuthedDTO user) {


    	List<User> userExist = UService.findByUsername(user.getEmail());
    	if (userExist.size()!=0) {
    		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Đăng kí không thành công, đã tồn tại tài email này", ""));
    	}
    	System.out.println("ok"+user.getEmail());
        if (user.getEmail().equals("") || user.getPassword().equals(""))
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Đăng kí không thành công", ""));

        Role initRole = roleRepo.findRoleByName("CANDIDATE");
        User newUser = new User(user.getEmail(), user.getPassword(), true, initRole);
        Candidate candidate = new Candidate();
        candidate.setEmail(user.getEmail());
        candidate.setUser(newUser);
        candidate.setFullName(user.getFullname());
        candidate.setAvatar("https://firebasestorage.googleapis.com/v0/b/jobhiringweb.appspot.com/o/avatars%2FavatarDefault.png?alt=media&token=caa9f8a4-ff38-4a35-a09b-23712bf2a504");
        UService.create(newUser);
        try {
            candidateService.save(candidate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Lỗi server !....", ""));

        }

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "Đăng kí thành công", ""));

    }


    @PostMapping(value = "/signup/employer", consumes = {"application/json"})
    public ResponseEntity<ResponseObject> createAccountHier(@RequestBody EmployeeAuthedDTO user) {

    	List<User> userExist = UService.findByUsername(user.getEmail());
    	if (userExist.size()!=0) {
    		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Đăng kí không thành công, đã tồn tại tài email này", ""));
    	}
        Role initRole = roleRepo.findRoleByName("EMPLOYER");
        User newUser = new User(user.getEmail(), user.getPassword(), false, initRole);

        Employer em = new Employer();
        Company com = new Company();


        em.setEmail(user.getEmail());
        em.setUser(newUser);
        em.setPhone(user.getPhone());
        em.setLogo("https://firebasestorage.googleapis.com/v0/b/jobhiringweb.appspot.com/o/avatars%2FavatarDefault.png?alt=media&token=caa9f8a4-ff38-4a35-a09b-23712bf2a504");
        System.out.println(user.getAddress());
        com.setAddress(user.getAddress());
        com.setRate((double) 5);
        com.setName(user.getName());
        com.setDomain(user.getDomain());
        com.setTaxCode(null);
        com.setLogo("https://firebasestorage.googleapis.com/v0/b/jobhiringweb.appspot.com/o/avatars%2FavatarDefault.png?alt=media&token=caa9f8a4-ff38-4a35-a09b-23712bf2a504");
        com.setEmployer(em);
        em.setCompany(com);

        try {
            UService.create(newUser);
            comService.save(com);
            emService.save(em);

            // gui mail
            EmailThreads emailThreads = new EmailThreads(com, em, environment, passEncoder, emailService);
            Thread thread = new Thread(emailThreads);
            thread.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Đăng không thành công", ""));

        }

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "Đăng kí thành công", ""));

    }


    @GetMapping("/verify")
    public ResponseEntity<ResponseObject> verifyEmail(@RequestParam(name = "id", defaultValue = "%") String id
            , @RequestParam(name = "token", defaultValue = "%") String token) {
        try {

			boolean check = passEncoder.matches(id, token);

            if (!check)
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseObject("failed", "verify failed", null));
            {

                Optional<User> u = UService.findById(Long.parseLong(id));

                u.get().setEnable(true);
                UService.save(u.get());

                return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("ok", "Xác thực thành công", null));

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseObject("failed", "Lỗi server !...", null));
        }

    }

//    @PostMapping("/forgotPassword")
//    public ResponseEntity<ResponseObject> forgotPassword(@RequestParam("email") String email, @RequestParam("role") String role) {
//        try {
//
//            List<User> userExist = UService.findByUsername(email);
//
//            if (userExist.size() == 0) {
//                return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("failed", "Tài khoản này không tồn tại", ""));
//            }
//
//
//            if(!check){
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseObject("failed", "Mật khẩu cũ đã nhập không đúng !", null));
//            }
//
//            user.setPassword(passwordEncoder.encode(newPassword));
//            User result = uService.save(user);
//
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject("ok", "Đổi mật khẩu thành công !", result));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
//                    .body(new ResponseObject("failed", "Lỗi server!...", null));
//        }
//    }

}
