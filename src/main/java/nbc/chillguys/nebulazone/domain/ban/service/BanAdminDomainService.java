package nbc.chillguys.nebulazone.domain.ban.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.ban.dto.BanCreateCommand;
import nbc.chillguys.nebulazone.domain.ban.dto.BanInfo;
import nbc.chillguys.nebulazone.domain.ban.entity.Ban;
import nbc.chillguys.nebulazone.domain.ban.exception.BanErrorCode;
import nbc.chillguys.nebulazone.domain.ban.exception.BanException;
import nbc.chillguys.nebulazone.domain.ban.repository.BanRepository;

@Service
@RequiredArgsConstructor
public class BanAdminDomainService {
	private final BanRepository banRepository;

	/**
	 * 밴 생성 로직
	 *
	 * @param command 밴 생성
	 * @author 정석현
	 */
	public void createBan(BanCreateCommand command) {
		validateNotAlreadyBanned(command.ipAddress());
		Ban ban = Ban.create(command);
		banRepository.save(ban);
	}

	/**
	 * 주어진 IP 주소에 해당하는 밴 정보를 삭제
	 *
	 * @param ipAddress 해제할 대상의 IP 주소
	 * @author 정석현
	 */
	@Transactional
	public void unban(String ipAddress) {
		findByIpAddress(ipAddress);
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

	/**
	 * 주어진 IP 주소에 해당하는 밴 정보를 조회하고, 없으면 예외를 발생
	 *
	 * @param ipAddress 조회할 IP 주소
	 * @throws BanException 존재하지 않는 경우
	 * @author 정석현
	 */
	public void findByIpAddress(String ipAddress) {
		banRepository.findByIpAddress(ipAddress)
			.orElseThrow(() -> new BanException(BanErrorCode.BAN_NOT_FOUND));
	}

	/**
	 * 이미 밴이 되었는지 확인
	 *
	 * @param ipAddress 아이피 주소
	 * @author 정석현
	 */
	public void validateNotAlreadyBanned(String ipAddress) {
		banRepository.findActiveBanByIp(ipAddress)
			.ifPresent(ban -> {
				throw new BanException(BanErrorCode.ALREADY_BANNED);
			});
	}
}
