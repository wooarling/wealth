package com.example.wealth.controller;


import com.example.wealth.model.Wealth;
import com.example.wealth.repository.WealthRepository;
import com.example.wealth.service.WealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
@Controller
public class WealthController {

    private final WealthRepository wealthRepository;

    public WealthController(WealthRepository wealthRepository) {
        this.wealthRepository = wealthRepository;
    }

    // select box 초기 페이지
    @GetMapping("/select-box")
    public String showSelectBox(Model model) {
        model.addAttribute("regions", wealthRepository.findDistinctCtprvnNm());
        model.addAttribute("districts", Map.of());
        model.addAttribute("results", List.of()); // 초기 검색 결과 없음
        return "select-box";
    }

    // 검색 결과
    @GetMapping("/select-box/results")
    public String searchResults(@RequestParam String ctprvnNm,
                                @RequestParam(required = false) String signguNm,
                                @RequestParam(required = false) String mrhstNm,
                                Model model) {

        List<Wealth> results;

        if (signguNm != null && !signguNm.isEmpty()) {
            if (mrhstNm != null && !mrhstNm.isEmpty()) {
                // 시도 + 군구 + 가맹점명 검색
                results = wealthRepository.findByCtprvnNmAndSignguNmAndMrhstNmContaining(
                        ctprvnNm, signguNm, mrhstNm);
            } else {
                // 시도 + 군구 검색
                results = wealthRepository.findByCtprvnNmAndSignguNm(ctprvnNm, signguNm);
            }
        } else {
            if (mrhstNm != null && !mrhstNm.isEmpty()) {
                // 시도 + 가맹점명 검색
                results = wealthRepository.findByCtprvnNmAndMrhstNmContaining(ctprvnNm, mrhstNm);
            } else {
                // 시도만 검색
                results = wealthRepository.findByCtprvnNm(ctprvnNm);
            }
        }

        model.addAttribute("results", results);
        model.addAttribute("selectedCtprvn", ctprvnNm);
        model.addAttribute("selectedSigngu", signguNm);
        model.addAttribute("enteredMrhst", mrhstNm); // 입력값 유지
        model.addAttribute("regions", wealthRepository.findDistinctCtprvnNm());

        return "select-box";
    }

    // AJAX: 군구 목록
    @GetMapping("/select-box/districts")
    @ResponseBody
    public List<String> getDistricts(@RequestParam String ctprvnNm) {
        return wealthRepository.findSignguByCtprvnNm(ctprvnNm);
    }
}



