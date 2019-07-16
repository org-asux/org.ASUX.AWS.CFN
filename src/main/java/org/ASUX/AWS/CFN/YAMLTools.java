/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.AWS.CFN;

import org.ASUX.yaml.YAML_Libraries;
import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgsBatchCmd;

import org.ASUX.YAML.NodeImpl.ReadYamlEntry;
import org.ASUX.YAML.NodeImpl.NodeTools;
import org.ASUX.YAML.NodeImpl.GenericYAMLScanner;
import org.ASUX.YAML.NodeImpl.GenericYAMLWriter;
import org.ASUX.YAML.NodeImpl.InputsOutputs;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.IOException;

import java.util.regex.*;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import org.junit.Test;
import static org.junit.Assert.*;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This enum class is a bit extensive, only because the ENNUMERATED VALUEs are strings.
 * For variations - see https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 */
public final class YAMLTools
{
    public static final String CLASSNAME = YAMLTools.class.getName();

    //-----------------------------
	public boolean verbose;
	private final ReadYamlEntry readcmd;
	private final DumperOptions dopt;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public YAMLTools( final boolean _verbose, final boolean _showStats, final DumperOptions _dopt ) {
		this.verbose = _verbose;
		this.dopt = _dopt;
        this.readcmd = new ReadYamlEntry( _verbose, _showStats, _dopt );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	public final ReadYamlEntry getReadcmd()		{ return this.readcmd; }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	// *  @param _readcmd a NotNull instance

    /**
     *  <p>Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,MyOrgName', 'AWS,MyEnvironment', ..</p>
     *  <p>Warning! if you are expecting a simple string, and you either make a mistake with "_YAMLPath" or .. the user enters much more than a simple string @ '_YAMLPath' .. you've got a problem!</p>
     *  @param _inputNode NotNull Node object
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     *  @return a Not-Null String (or else a runtime-assertion-exception is thrown, as determined within {@link #getScalarContent}
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, or the '_YAMLPath' does not point to a simple String
     */
    public String readStringFromYAML( final Node _inputNode, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readStringFromYAML(<Node>, "+ _YAMLPath +"): ";
        this.readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final Node output = this.readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        final String s = getScalarContent( output, _YAMLPath );
        return s;
    }

    //=================================================================================
    /**
     *  Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,VPC,subnet,SERVERS,?MyEC2InstanceName?",yum', 'AWS,VPC,subnet,SERVERS,?MyEC2InstanceName?,configCustomCommands'..
     *  @param _inputNode NotNull Node object
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     *  @return a possibly-Null Node (or else a runtime-assertion-exception is thrown, as determined within {@link #getScalarContent}
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    public Node readNodeFromYAML( final Node _inputNode, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readNodeFromYAML(<Node>, "+ _YAMLPath +"): ";
        this.readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final SequenceNode output = this.readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        final java.util.List<Node> seqs = output.getValue();
        if ( seqs.size() <= 0 )
            return null;
        else
            return seqs.get(0);
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Assumes that the Node 'YAML-tree' passed in actually either a simple ScalarNode, or a SequenceNode with just one ScalarNode.</p>
     *  <p>If its not a valid assumption, either an Exception or an Assertion-RuntimeException is thrown</p>
     *  @param _n a NotNull Node object
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String (to be used exclusively for describing any Exception to the user)
     *  @return a simple String
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided.
     */
    public String getScalarContent( final Node _n, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": getScalarContent(): ";
        if ( this.verbose ) System.out.println( HDR +" provided argument =\n" + NodeTools.Node2YAMLString( _n ) + "\n");
        assertTrue( _n != null );
        if ( _n instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) _n;
            return scalar.getValue();
        } else if ( _n instanceof SequenceNode ) {
            final SequenceNode seqNode = (SequenceNode) _n;
            final java.util.List<Node> seqs = seqNode.getValue();
            assertTrue( seqs.size() == 1 );
            assertTrue( seqs.get(0) instanceof ScalarNode );
            final ScalarNode scalar = (ScalarNode) seqs.get(0);
            return scalar.getValue();
        } else {
            throw new Exception( "Must provide a simple 'scalar' string @ the YAML-Path: "+ _YAMLPath +"\nInvalid Node of type: "+ _n.getNodeId() +"\nInstead, you provided:\n"+ NodeTools.Node2YAMLString( _n ) );
        }
    }


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};