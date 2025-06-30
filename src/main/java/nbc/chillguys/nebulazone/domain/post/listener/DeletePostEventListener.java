package nbc.chillguys.nebulazone.domain.post.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.event.DeletePostEvent;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;

@RequiredArgsConstructor
@Component
public class DeletePostEventListener {

	private final PostDomainService postDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDeletePost(DeletePostEvent event) {
		postDomainService.deletePostFromEs(event.postId());
	}
}
