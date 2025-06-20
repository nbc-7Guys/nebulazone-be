package nbc.chillguys.nebulazone.domain.ban.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.ban.dto.BanInfo;
import nbc.chillguys.nebulazone.domain.ban.repository.BanRepository;

@Service
@RequiredArgsConstructor
public class BanAdminDomainService {
	private final BanRepository banRepository;

	/**
	 * 주어진 IP 주소에 해당하는 밴 정보를 삭제
	 *
	 * @param ipAddress 해제할 대상의 IP 주소
	 * @author 정석현
	 */
	public void unban(String ipAddress) {
		banRepository.deleteByIpAddress(ipAddress);
	}

	/**
	 * 전체 밴된 IP 목록을 조회하여 도메인 전용 DTO(BanInfo) 리스트로 반환
	 *
	 * @return 밴 정보 리스트 (BanInfo)
	 * @author 정석현
	 */
	public List<BanInfo> findBanInfos() {
		return banRepository.findAll().stream()
			.map(BanInfo::from)
			.toList();
	}
}
