package nbc.chillguys.nebulazone.domain.post.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.event.CreatePostEvent;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;

@RequiredArgsConstructor
@Component
public class CreatePostEventListener {

	private final PostDomainService postDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUpdatePost(CreatePostEvent event) {
		postDomainService.savePostToEs(event.post());
	}
}
