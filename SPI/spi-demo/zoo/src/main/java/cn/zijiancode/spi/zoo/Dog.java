package cn.zijiancode.spi.zoo;

import cn.zijiancode.spi.provider.Animal;

/**
 * 狗子
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Dog implements Animal {

    @Override
    public void call() {
        System.out.println("汪汪汪!!!");
    }
}
