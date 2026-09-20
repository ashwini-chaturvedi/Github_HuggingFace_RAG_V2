package backend.hugitbackend.component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final TextEncryptor textEncryptor;

    @Override
    public String convertToDatabaseColumn(String plainText) {
        return plainText==null?null:this.textEncryptor.encrypt(plainText);
    }

    @Override
    public String convertToEntityAttribute(String cipherTextFromDB) {
        return cipherTextFromDB==null?null:this.textEncryptor.encrypt(cipherTextFromDB);
    }
}
