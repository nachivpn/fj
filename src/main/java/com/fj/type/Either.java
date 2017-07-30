/**
Contributors: Nachi
 */
package com.fj.type;

import java.util.function.Function;
/**
 * A simple data type for constructing a disjoint union (or sum type) of two types
 * 
 * @author nachivpn 
 * @param <L>
 * @param <R>
 */
public abstract class Either<L, R>{

    public abstract <T> T match(Function<L, T> a, Function<R, T> b);
    
    public abstract <S> Either<L, S> bind(Function<R, Either<L, S>> f);
    
    public abstract <S> Either<L,S> fmap(Function<R, S> f);

    public final static class Left<LL, LR> extends Either<LL, LR>{

        LL l;
        
        public Left(LL l) {
            this.l = l;
        }

        @Override
        public <T> T match(Function<LL, T> a, Function<LR, T> b) {
            return a.apply(l);
        }

        @Override
        public <S> Either<LL, S> bind(Function<LR, Either<LL, S>> f) {
            return new Left<>(l);
        }

        @Override
        public <S> Either<LL, S> fmap(Function<LR, S> f) {
            return new Left<>(l);
        }

    }
    
    public final static class Right<RL,RR> extends Either<RL, RR>{

        RR r;
        
        public Right(RR r) {
            this.r = r;
        }

        @Override
        public <T> T match(Function<RL, T> a, Function<RR, T> b) {
            return b.apply(r);
        }

        @Override
        public <S> Either<RL, S> bind(Function<RR, Either<RL, S>> f) {
            return f.apply(r);
        }

        @Override
        public <S> Either<RL, S> fmap(Function<RR, S> f) {
            return new Right<>(f.apply(r));
        }
        
    }
    
}