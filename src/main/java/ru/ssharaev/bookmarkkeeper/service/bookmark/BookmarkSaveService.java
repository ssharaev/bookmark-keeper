package ru.ssharaev.bookmarkkeeper.service.bookmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.ssharaev.bookmarkkeeper.model.Bookmark;
import ru.ssharaev.bookmarkkeeper.model.BookmarkType;
import ru.ssharaev.bookmarkkeeper.repository.BookmarkRepository;

import static ru.ssharaev.bookmarkkeeper.TelegramMessageUtils.fetchEntityValue;
import static ru.ssharaev.bookmarkkeeper.TelegramMessageUtils.hasEntity;

/**
 * Закладываемся на то, что в сервис передается сообщение без команд,
 * только с закладкой, которую нужно сохранить
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookmarkSaveService {
    private final BookmarkTagProvider tagProvider;
    private final BookmarkRepository bookmarkRepository;

    public Bookmark saveBookmark(Message message) {
        log.info("Сохраняем сообщение");
        Bookmark bookmark = createBookmark(message);
        bookmarkRepository.saveBookmark(bookmark);
        log.info("Saved bookmark: {}", bookmark);
        return bookmark;
    }

    public Bookmark createBookmark(Message message) {
        return new Bookmark(
                message.getMessageId().toString(),
                getBookmarkType(message),
                "Test",
                getUrl(message),
                message.getText(),
                tagProvider.fetchTag(message)
        );
    }

    private String getUrl(Message message) {
        return hasEntity(message, EntityType.URL) ? fetchEntityValue(message, EntityType.URL) : null;
    }

    /**
     * Определяет тип закладки. Пока поддерживаем 4 типа - текст, фото, ссылка, документ
     *
     * @param message сообщение клиента
     * @return тип закладки
     */
    private BookmarkType getBookmarkType(Message message) {
        if (!message.hasEntities()) {
            if (message.hasDocument()) {
                return BookmarkType.FILE;
            }
            if (message.hasPhoto()) {
                return BookmarkType.PHOTO;
            }
            return BookmarkType.TEXT;
        }
        return hasEntity(message, EntityType.URL) ? BookmarkType.URL : BookmarkType.TEXT;
    }
}
