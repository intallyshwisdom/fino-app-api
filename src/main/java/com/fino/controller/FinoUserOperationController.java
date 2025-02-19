package com.fino.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fino.dto.ClientDetailsDto;
import com.fino.dto.ClientSearchDto;
import com.fino.dto.FinoUserDetailsDto;
import com.fino.dto.FinoUserEdit;
import com.fino.helpers.AuthorizationHelpers;
import com.fino.service.ClientDetailsService;
import com.fino.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/fino/system/user")
public class FinoUserOperationController {

	@Autowired
	private UserService userService;

	@Autowired
	private ClientDetailsService clientDetailsService;

	@PostMapping("/sign-up")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> userSignUp(@RequestBody @Valid FinoUserDetailsDto finoUserDetailsDto) {

		return ResponseEntity.ok(this.userService.insertFinoUserDetails(finoUserDetailsDto));
	}

	@GetMapping("/get-all-users")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> getAllUsers() {
		return ResponseEntity.ok(this.userService.getAllFinoUsersDetails());
	}

	@PostMapping("/assign-a-new-role")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> assignRoles(
			@RequestParam(name = "mobileNumber", required = true) String mobileNumber,
			@RequestParam(name = "userRole", required = true) String userRole) {
		return ResponseEntity.ok(this.userService.assignANewRole(mobileNumber, userRole));
	}

	@DeleteMapping("/delete-previous-role")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> deleteAssignedRoles(
			@RequestParam(name = "roleId", required = true) Long roleId) {
		return ResponseEntity.ok(this.userService.deletePreviousAssignedRole(roleId));
	}

	@PostMapping("/reset-password")
	@PreAuthorize(AuthorizationHelpers.USER_AUTH)
	public ResponseEntity<Map<Object, Object>> resetPassword(
			@RequestParam(name = "mobileNumber", required = true) String mobileNumber,
			@RequestParam(name = "oldPassword", required = true) String oldPassword,
			@RequestParam(name = "newPassword", required = true) String newPassword) {
		return ResponseEntity.ok(this.userService.changePassword(mobileNumber, oldPassword, newPassword));
	}

	@DeleteMapping("/disable-an-user")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> disableAnUser(
			@RequestParam(name = "mobileNumber", required = true) String mobileNumber) {
		return ResponseEntity.ok(this.userService.deleteFinoUserDetails(mobileNumber));
	}

	@PutMapping("/update-fino-user")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> updateAnUser(
			@RequestParam(name = "mobileNumber", required = true) String mobileNumber,
			@RequestBody FinoUserEdit finouserEdit) {
		System.out.println(finouserEdit);
		return ResponseEntity.ok(this.userService.updateFinoUserDetails(mobileNumber, finouserEdit));
	}

	@PostMapping("/add-a-new-client")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> addClient(@RequestBody ClientDetailsDto clientDetailsDto) {
		return ResponseEntity.ok(this.clientDetailsService.insertClientDetails(clientDetailsDto));
	}

	@DeleteMapping("/delete-a-new-client/{clientId}")
	@PreAuthorize(AuthorizationHelpers.ADMIN_AUTH)
	public ResponseEntity<Map<Object, Object>> deleteClient(@PathVariable("clientId") Long clientId) {
		return ResponseEntity.ok(this.clientDetailsService.deleteClientDetails(clientId));
	}
	@GetMapping("/get-all-clients")
	@PreAuthorize(AuthorizationHelpers.USER_AUTH)
	public ResponseEntity<Map<Object, Object>> getAllClients() {
		return ResponseEntity.ok(this.clientDetailsService.getAllClientDetails());
	}
	
	@PostMapping("/get-cms-txn-by-client/{mobileNumber}")
	@PreAuthorize(AuthorizationHelpers.CLIENT_AUTH)
	public ResponseEntity<Map<Object, Object>> getCmsTxnByMobileNumber(@PathVariable("mobileNumber") String mobileNumber,@RequestBody ClientSearchDto clientSearchDto) {
		return ResponseEntity.ok(this.clientDetailsService.getClientTransactionByUserName(mobileNumber,clientSearchDto));
	}
	
}
