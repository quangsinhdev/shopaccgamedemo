package com.shopaccgame.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopaccgame.dtos.gameaccount.GameAccountDTO;
import com.shopaccgame.services.gameaccount.FifaService;
import com.shopaccgame.services.gameaccount.LOLService;
import com.shopaccgame.services.gameaccount.LQMService;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/gameaccounts")
@Tag(name = "Game Account API", description = "APIs related to Game account display list")
public class GameAccountController {

	private final FifaService fifaService;
	private final LOLService lolService;
	private final LQMService lqmService;

	public GameAccountController(FifaService fifaService, LOLService lolService, LQMService lqmService) {
		this.fifaService = fifaService;
		this.lolService = lolService;
		this.lqmService = lqmService;
	}

	@Operation(summary = "Get a list containing all FIFA Game accounts for sale", description = "Get a list containing all FIFA Game accounts saling")
	@GetMapping("/fifa")
	public ResponseEntity<Page<GameAccountDTO>> getFifaAccountsSelling(
			@RequestParam(value = "page", defaultValue = "0") String pageStr,
			@RequestParam(value = "size", defaultValue = "9") String sizeStr,
			@RequestParam(value = "sort", defaultValue = "desc") String sort) {
		int page = parsePage(pageStr);
		int size = parseSize(sizeStr);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), "price"));

		Page<GameAccountDTO> fifaAccounts = fifaService.getAllFifaAccountsSelling(pageable)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));
		return ResponseEntity.ok(fifaAccounts);
	}

	@Operation(summary = "Get a list containing all LOL Game accounts for sale", description = "Get a list containing all LOL Game accounts saling")
	@GetMapping("/lmht")
	public ResponseEntity<Page<GameAccountDTO>> getLOLAccountsSelling(
			@RequestParam(value = "page", defaultValue = "0") String pageStr,
			@RequestParam(value = "size", defaultValue = "9") String sizeStr,
			@RequestParam(value = "sort", defaultValue = "desc") String sort) {
		int page = parsePage(pageStr);
		int size = parseSize(sizeStr);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), "price"));

		Page<GameAccountDTO> lolAccounts = lolService.getAllLOLAccountsSelling(pageable)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));
		return ResponseEntity.ok(lolAccounts);
	}

	@Operation(summary = "Get a list containing all LQM Game accounts for sale", description = "Get a list containing all LQM Game accounts saling")
	@GetMapping("/lqm")
	public ResponseEntity<Page<GameAccountDTO>> getLQAccountsSelling(
			@RequestParam(value = "page", defaultValue = "0") String pageStr,
			@RequestParam(value = "size", defaultValue = "9") String sizeStr,
			@RequestParam(value = "sort", defaultValue = "desc") String sort) {
		int page = parsePage(pageStr);
		int size = parseSize(sizeStr);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), "price"));

		Page<GameAccountDTO> lqmAccounts = lqmService.getAllLQMAccountsSelling(pageable)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));
		return ResponseEntity.ok(lqmAccounts);
	}

	@Operation(summary = "Get a list containing all Game accounts for sale", description = "Get a list containing all Game accounts saling (includes all categories)")
	@GetMapping("/all")
	public ResponseEntity<Page<GameAccountDTO>> getAllAccounts(
			@RequestParam(value = "page", defaultValue = "0") String pageStr,
			@RequestParam(value = "size", defaultValue = "9") String sizeStr,
			@RequestParam(value = "sort", defaultValue = "desc") String sort) {
		int page = parsePage(pageStr);
		int size = parseSize(sizeStr);
		Pageable pageableForService = PageRequest.of(0, Integer.MAX_VALUE,
				Sort.by(Sort.Direction.fromString(sort), "price"));

		Page<GameAccountDTO> fifaAccounts = fifaService.getAllFifaAccountsSelling(pageableForService)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));
		Page<GameAccountDTO> lolAccounts = lolService.getAllLOLAccountsSelling(pageableForService)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));
		Page<GameAccountDTO> lqmAccounts = lqmService.getAllLQMAccountsSelling(pageableForService)
				.map(account -> EntityConvertToDTOUtil.convertToDTO(account, GameAccountDTO.class));

		List<GameAccountDTO> allAccounts = new ArrayList<>();
		allAccounts.addAll(fifaAccounts.getContent());
		allAccounts.addAll(lolAccounts.getContent());
		allAccounts.addAll(lqmAccounts.getContent());

		allAccounts.sort((a, b) -> sort.equalsIgnoreCase("desc") ? Double.compare(b.getPrice(), a.getPrice())
				: Double.compare(a.getPrice(), b.getPrice()));

		Pageable pageableForResponse = PageRequest.of(page, size);
		int start = (int) pageableForResponse.getOffset();
		int end = Math.min(start + pageableForResponse.getPageSize(), allAccounts.size());
		List<GameAccountDTO> pagedAccounts = allAccounts.subList(start, end);

		long totalElements = fifaAccounts.getTotalElements() + lolAccounts.getTotalElements()
				+ lqmAccounts.getTotalElements();
		Page<GameAccountDTO> result = new PageImpl<>(pagedAccounts, pageableForResponse, totalElements);

		return ResponseEntity.ok(result);
	}

	private int parsePage(String pageStr) {
		try {
			int page = Integer.parseInt(pageStr);
			return page < 0 ? 0 : page;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private int parseSize(String sizeStr) {
		try {
			int size = Integer.parseInt(sizeStr);
			return size <= 0 ? 9 : size;
		} catch (NumberFormatException e) {
			return 9;
		}
	}
}