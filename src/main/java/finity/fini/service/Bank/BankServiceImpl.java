package finity.fini.service.Bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import finity.fini.converter.BankConverter;
import finity.fini.domain.Bank;
import finity.fini.dto.Bank.BankResponseDTO;
import finity.fini.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankServiceImpl implements BankService{

    private final BankRepository bankRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fss.api.key}")
    private String fssApiKey;

    @Value("${fss.api.url.banks}")
    private String fssBankUrl;

    @Override
    @Transactional
    public void syncBanks() {
        String url = String.format("%s?auth=%s&topFinGrpNo=020000&pageNo=1", fssBankUrl, fssApiKey);
        log.info("Requesting FSS Bank List API URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("금융감독원 [금융회사 목록] API 응답 수신 완료. Status: {}", responseEntity.getStatusCode());

            if (responseEntity.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(responseEntity.getBody())) {
                throw new RuntimeException("금융감독원 [금융회사 목록] API로부터 비정상 응답을 받았습니다. Status: " + responseEntity.getStatusCode());
            }

            // BankResponseDTO를 사용하여 JSON 파싱
            BankResponseDTO responseDto = objectMapper.readValue(responseEntity.getBody(), BankResponseDTO.class);

            if (responseDto == null || responseDto.getResult() == null) {
                throw new RuntimeException("금융감독원 [금융회사 목록] API 응답 파싱 후 데이터가 비어있습니다.");
            }

            List<BankResponseDTO.BankDto> bankDtos = responseDto.getResult().getBaseList();

            int newBankCount = 0;
            for (BankResponseDTO.BankDto bankDto : bankDtos) {
                // DB에 해당 은행 코드가 존재하지 않을 경우에만 저장
                if (!bankRepository.existsById(bankDto.getFinCoNo())) {
                    Bank bank = BankConverter.toBank(bankDto);
                    bankRepository.save(bank);
                    log.info("{} 정보 신규 저장 완료.", bank.getKorCoNm());
                    newBankCount++;
                }
            }
            log.info("총 {}개의 [금융회사] 정보 중 {}개를 신규로 DB에 저장 완료", bankDtos.size(), newBankCount);

        } catch (Exception e) {
            log.error("금융감독원 [금융회사 목록] API 동기화 중 오류 발생", e);
            throw new RuntimeException("금융감독원 [금융회사 목록] API 동기화 처리 중 오류가 발생했습니다.", e);
        }

    }
}
