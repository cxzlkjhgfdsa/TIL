package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();

    }

    @Test
    void checked_throw(){
        Service service = new Service();
        Assertions.assertThrows(MyCheckedException.class, ()->service.callThrow());

    }

    /*
        Exception을 상속받은 예외는 체크 예외가 된다.
     */
    static class MyCheckedException extends Exception{
        public MyCheckedException(String message){
            super(message);
        }
    }
    
    /*
        *check 예외는 잡아서 던지거나 처리하거나 둘중 하나 해야함
     */
    static class Service{
        Repository repository = new Repository();

        /*
            에외를 잡아서 처리하는 코드
         */
        public void callCatch(){
            try {
                repository.call();
            } catch (MyCheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }
        
        /*
         체크 예외를 밖으로 던지는 코드
         박으로 던지려면 메소드에 필수로 던지는 코드를 선언해야함
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository{
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }
}
