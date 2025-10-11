package finity.fini.data;

import finity.fini.domain.Bank;
import finity.fini.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final BankRepository bankRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>>> 데이터 로더 시작: 은행 정보 확인 및 저장 <<<<");

        List<Bank> banks = Arrays.asList(
                Bank.builder().finCoNo("0010001").korCoNm("우리은행").build(),
                Bank.builder().finCoNo("0010002").korCoNm("한국스탠다드차타드은행").build(),
                Bank.builder().finCoNo("0010016").korCoNm("아이엠뱅크").build(),
                Bank.builder().finCoNo("0010017").korCoNm("부산은행").build(),
                Bank.builder().finCoNo("0010019").korCoNm("광주은행").build(),
                Bank.builder().finCoNo("0010020").korCoNm("제주은행").build(),
                Bank.builder().finCoNo("0010022").korCoNm("전북은행").build(),
                Bank.builder().finCoNo("0010024").korCoNm("경남은행").build(),
                Bank.builder().finCoNo("0010026").korCoNm("중소기업은행").build(),
                Bank.builder().finCoNo("0010030").korCoNm("한국산업은행").build(),
                Bank.builder().finCoNo("0010927").korCoNm("국민은행").build(),
                Bank.builder().finCoNo("0011625").korCoNm("신한은행").build(),
                Bank.builder().finCoNo("0013175").korCoNm("농협은행주식회사").build(),
                Bank.builder().finCoNo("0013909").korCoNm("주식회사 하나은행").build(),
                Bank.builder().finCoNo("0014674").korCoNm("주식회사 케이뱅크").build(),
                Bank.builder().finCoNo("0014807").korCoNm("수협은행").build(),
                Bank.builder().finCoNo("0015130").korCoNm("주식회사 카카오뱅크").build(),
                Bank.builder().finCoNo("0017801").korCoNm("토스뱅크 주식회사").build()
        );

        for (Bank bank : banks) {
            // DB에 해당 은행 코드가 존재하지 않을 경우에만 저장
            if (!bankRepository.existsById(bank.getFinCoNo())) {
                bankRepository.save(bank);
                log.info("{} 정보 저장 완료.", bank.getKorCoNm());
            }
        }

        log.info(">>>> 데이터 로더 종료: 은행 정보 초기화 완료 <<<<");
    }
}