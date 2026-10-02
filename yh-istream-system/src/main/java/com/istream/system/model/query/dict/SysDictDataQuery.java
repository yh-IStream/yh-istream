package com.istream.system.model.query.dict;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictDataQuery extends BaseQuery {

    private String dictType;

    private String dictLabel;
}