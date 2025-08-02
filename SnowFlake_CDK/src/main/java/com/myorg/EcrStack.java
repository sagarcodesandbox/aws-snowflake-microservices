package com.myorg;
import software.amazon.awscdk.App;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.RepositoryProps;
import software.amazon.awscdk.services.ecr.TagMutability;

public class EcrStack extends Stack {

    private final Repository productServiceRepository;

    public EcrStack(final App app, final String id, final StackProps props) {
        super(app, id, props);
        this.productServiceRepository = new Repository(this, "SnowFlakeProductService",
                RepositoryProps.builder()
                        .repositoryName("snowflake-product-service")
                        .removalPolicy(RemovalPolicy.DESTROY)
                        .imageTagMutability(TagMutability.IMMUTABLE)
                        .autoDeleteImages(true)
                .build()
        );
    }

    public Repository getProductServiceRepository() {
        return productServiceRepository;
    }

}