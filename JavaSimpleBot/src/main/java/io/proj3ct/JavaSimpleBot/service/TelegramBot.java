package io.proj3ct.JavaSimpleBot.service;

import io.proj3ct.JavaSimpleBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    BotConfig config;

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> commandsList = new ArrayList<>();
        commandsList.add(new BotCommand("/start", "Начать работу"));

        try {
            this.execute(new SetMyCommands(commandsList, new BotCommandScopeDefault(), null));
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            Translator translator = new Translator();
            String answer = null;

            if (messageText.equals("/start")){
                startCommandReceived(chatId, userName);
            }
            if (messageText.matches("^[^а-яА-Я]+$") && !messageText.equals("/start")) {
                try {
                    answer = translator.translate("en", "ru",messageText);
                } catch (IOException e) {
                    sendMessage(chatId, "Произошла ошибка. Попробуйте снова");
                }
                sendMessage(chatId, answer);
            }
            if (messageText.matches("^[^a-zA-Z]+$")) {
                try {
                    answer = translator.translate("ru", "en",messageText);
                } catch (IOException e) {
                    sendMessage(chatId, "Произошла ошибка. Попробуйте снова");
                }
                sendMessage(chatId, answer);
            }
        }
    }
    private void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name + "!!! Я простой бот переводчик, который переводит входящие сообщения " +
                "с английского на русский и наоборот. Я сам определю язык входящего сообщения, поэтому для перевода " +
                "текста просто напишите мне.\nПервод осуществляется сервисом «google translate»" +
                "https://translate.google.com/?hl=ru";
        sendMessage(chatId,answer);
    }
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
        }catch (TelegramApiException e){
            sendMessage(chatId, "Произошла ошибка. Попробуйте снова");
        }
    }
}