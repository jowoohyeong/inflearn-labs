package hello.proxy.pureproxy.concreteporxy;

import hello.proxy.pureproxy.concreteporxy.code.ConcreteClient;
import hello.proxy.pureproxy.concreteporxy.code.ConcreteLogic;
import hello.proxy.pureproxy.concreteporxy.code.TimeProxy;
import org.junit.jupiter.api.Test;

public class ConcreteProxyTest {
    @Test
    void noProxy() {
        ConcreteLogic concreteLogic = new ConcreteLogic();
        ConcreteClient client = new ConcreteClient(concreteLogic);
        client.execute();
    }

    @Test
    void addProxy() {
        ConcreteLogic concreteLogic = new ConcreteLogic();
        TimeProxy timeProxy = new TimeProxy(concreteLogic);
        ConcreteClient client = new ConcreteClient(timeProxy);
        client.execute();
    }
}
