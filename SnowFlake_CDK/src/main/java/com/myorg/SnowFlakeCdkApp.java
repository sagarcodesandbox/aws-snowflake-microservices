package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SnowFlakeCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment environment = Environment.builder()
                        .account("795438191019")
                        .region("ap-south-1")
                        .build();
        Map<String,String> infraTags = new HashMap<>();
        infraTags.put("team", "microservicescodesandbox");
        infraTags.put("tag2", "EcommerceInfra");

        EcrStack ecrStack = new EcrStack(
                app,
                "Ecr",
                StackProps.builder()
                        .env(environment)
                        .tags(infraTags)
                        .build());

        VPCStack vpcStack = new VPCStack(app, "Vpc", StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build());

        ClusterStack clusterStack = new ClusterStack(app, "Cluster", StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build(), new ClusterStackProps(vpcStack.getVpc()));
        clusterStack.addDependency(vpcStack); //added depencencyfdfdsdsddfdk



        app.synth();
    }
}

