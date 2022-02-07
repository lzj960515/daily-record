package cn.zijiancode.spi.zoo;

import cn.zijiancode.spi.provider.Animal;

/**
 * 猫咪
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Cat implements Animal {
    @Override
    public void call() {
        System.out.println("喵喵喵～～");
    }
}
