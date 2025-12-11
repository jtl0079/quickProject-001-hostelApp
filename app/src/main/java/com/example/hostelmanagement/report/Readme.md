# structure
```agsl

report/
│
├── domain/
│   ├── model/               # HostelReport (领域实体)
│   ├── repository/          # IHostelReportRepository (接口)
│   └── mapper/              # domain <-> dto 转换器
│
├── infrastructure/
│   ├── dto/
│   │   ├── mapper/
│   │   │   ├── HostelReportDtoMapper.kt
│   │   │   ├── AllTimeDataBundleDtoMapper.kt
│   │   │   └── TimeEntryDtoMapper.kt
│   │   └── model/
│   │       ├── HostelReportDto.kt
│   │       ├── AllTimeDataBundleDto.kt
│   │       └── TimeEntryDto.kt
│   ├── datasource/          # Firebase 数据源
│   │   └── HostelReportDataSourceFirestore.kt
│   └── repository/
│       └── HostelReportRepositoryImpl.kt
│
└── usecase/
    ├── GetReportsByBranchUseCase.kt
    ├── ObserveReportsByBranchUseCase.kt
    └── SaveReportUseCase.kt
```