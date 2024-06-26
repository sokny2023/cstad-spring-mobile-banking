package co.istad.banking.base;

import lombok.*;
import org.springframework.stereotype.Service;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasedMassage<T> {
    T message;
}
