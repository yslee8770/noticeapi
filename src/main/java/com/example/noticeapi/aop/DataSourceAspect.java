package com.example.noticeapi.aop;

import com.example.noticeapi.config.DataSourceContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
public class DataSourceAspect {

  @Before("@annotation(transactional) && execution(* com.example.noticeapi.service..*(..))")
  public void beforeTransactionalMethod(Transactional transactional) {
    if (transactional.readOnly()) {
      DataSourceContextHolder.setDataSourceType("slave");
    } else {
      DataSourceContextHolder.setDataSourceType("master");
    }
  }

  @After("@annotation(transactional) && execution(* com.example.noticeapi.service..*(..))")
  public void afterTransactionalMethod(Transactional transactional) {
    DataSourceContextHolder.clearDataSourceType();
  }
}
