package hello.core.scope;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;

public class PrototypeProviderTest {

    @Test
    void providerTest() {
        AnnotationConfigApplicationContext ac =
                new AnnotationConfigApplicationContext(ClientBean.class, PrototypeBean.class);

        ClientBean cb1 = ac.getBean(ClientBean.class);
        int count1 = cb1.logic();
        assertThat(count1).isEqualTo(1);

        ClientBean cb2 = ac.getBean(ClientBean.class);
        int count2 = cb2.logic();
        assertThat(count2).isEqualTo(1);
    }


    static class ClientBean {
        @Autowired
        private ObjectProvider<PrototypeBean> prototypeBeanProvider;

        public int logic() {
            PrototypeBean pb = prototypeBeanProvider.getObject();
            pb.addCount();
            int count = pb.getCount();
            return count;
        }
    }

    @Scope("prototype")
    static class PrototypeBean {
       private int count = 0;

       public void addCount() {
           count++;
       }

       public int getCount() {
           return count;
       }

       @PostConstruct
        public void init() {
           System.out.println("PrototypeBean.init" + this);
       }

       @PreDestroy
        public void destroy() {
           System.out.println("PrototypeBean.destroy");
       }
    }
}
