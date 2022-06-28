import React, {useEffect, useState} from 'react'
import * as d3 from 'd3'
import LayerData from '../structure/LayerData'
import Edge from '../structure/Edge'
import NeuronData from '../structure/NeuronData'
import './style/Panel.scss'
import * as api from 'nnbuilder-api'
import incDecInput from './IncDecInput'
import LayersSettings from './LayersSettings'
import ProjectInfo from '../structure/ProjectInfo'
import {useStore} from '@nanostores/react'
import {currentVersion} from './App'

type EditorWindowProps = {
    modificationService: api.NNModificationServicePromiseClient
    versionService: api.NNVersionServicePromiseClient

    projectInfo: ProjectInfo
}

function calculateEdges(layers: LayerData[]): Edge[] {
    let prev: LayerData | undefined = undefined
    return layers.flatMap((layer) => {
        let result: Edge[] = []
        if (prev !== undefined) {
            result = prev.neurons.flatMap((from) => layer.neurons
                .flatMap((to) => new Edge(from, to)))
        }
        prev = layer
        return result
    })
}

function EditorWindow(props: EditorWindowProps) {
    const [layers, setLayers] = useState<LayerData[]>([])
    const edges = calculateEdges(layers)
    const project = useStore(currentVersion)

    useEffect(() => {
        setLayers(props.projectInfo.layerData)
    }, [])

    const width = (layerData: LayerData) => {
        return 300 + (700 - 300) * layerData.neurons.length / 100
    }
    const center = () => {
        return ((800 - 10) / 2)
    }

    d3.select('#layers').select('g.layers')
        .selectAll('rect')
        .data(layers)
        .join('rect')
        .attr('x', (layerData) => center() - width(layerData) / 2)
        .attr('y', (layerData) => layerData.id * 115)
        .attr('width', (layerData) => width(layerData))
        .attr('height', 100)
        .attr('rx', 5)
        .attr('ry', 5)
        .attr('fill', '#D9D9D9')

    const edgesContainer = d3.select('#layers').select('g.graph')
        .selectAll('line')
        .data(edges)
        .join('line')
        .attr('x1', (d) => d.from.x)
        .attr('y1', (d) => d.from.y)
        .attr('x2', (d) => d.to.x)
        .attr('y2', (d) => d.to.y)
        .attr('stroke', '#D95555')

    const neuronsContainer = d3.select('#layers').select('g.graph')
        .selectAll('circle')
        .data(layers.flatMap((x) => x.neurons))
        .join('circle')
        .attr('r', 3)
        .attr('x', 150)
        .attr('y', (d: NeuronData) => d.layer_id * 115 + 50)
        .attr('fill', '#D95555')

    const simulation = d3.forceSimulation(layers.flatMap((x) => x.neurons))
        .force('collideForce', d3.forceCollide().radius(10).strength(0.4))
        .force('x', d3.forceX(function() {
            return 400
        }).strength(0.025),
        )
        .force('y', d3.forceY(function(d: NeuronData) {
            return d.layer_id * 115 + 50
        }).strength(1))
        .alphaDecay(0.01)

    simulation.on('tick', () => {
        neuronsContainer
            .attr('cx', (d) => d.x)
            .attr('cy', (d) => d.y)
        edgesContainer
            .attr('x1', (d) => d.from.x)
            .attr('y1', (d) => d.from.y)
            .attr('x2', (d) => d.to.x)
            .attr('y2', (d) => d.to.y)
    })

    useEffect(() => {
        // call api and get layers
        // setLayers([new LayerData(0)])

        const svg = d3.select('#layers')
            .append('svg')
            .attr('style', 'width: 100%; height: 100%')
        svg.append('g').attr('class', 'layers')
        svg.append('g').attr('class', 'graph')
    }, [])

    function add(n = 1) {
        const layersId = layers.length
        const newLayers = [...Array(n).keys()].map((i) => new LayerData(layersId + i))
        console.log(newLayers)

        const requests = newLayers.map((layer) =>
            new api.NNModificationRequest().setNnid(Number(project)).setAddlayer(new api.AddLayer()
                .setIndex(Number(layer.id)).setLtype(layer.type)),
        ).reverse()
        makeRequest(requests.pop()!, requests, () => {
            setLayers((prev) => prev.concat(newLayers))
        })
    }

    function makeRequest(request: api.NNModificationRequest, other: api.NNModificationRequest[], action: () => void) {
        props.modificationService.modifynn(request).then(() => {
            const last = other.pop()
            if (last != undefined) {
                makeRequest(last, other, action)
            } else {
                action()
            }
        })
    }

    function remove(n = 1) {
        const layersAmount = layers.length
        const ids = [...Array(n).keys()].map((i) => layersAmount - i - 1)
        const requests = ids.map((id) =>
            new api.NNModificationRequest().setNnid(Number(project)).setDellayer(new api.AddLayer()
                .setIndex(id)),
        ).reverse()
        makeRequest(requests.pop()!, requests, () => {
            setLayers((prev) => prev.slice(0, -n))
        })
    }

    const neuronsStyle = {width: 1000, height: layers.length * 115 + 'px'}

    const setLayersAmount = (amount: number) => {
        const change = amount - layers.length
        change < 0 ? remove(-change) : add(change)
    }

    const updateLayer = (layer: LayerData, i: number) => {
        setLayers((prev) => {
            const result = new Array(...prev)
            result[i] = layer
            return result
        })
    }

    return (
        <div className='editor'>
            <div className={'layers-amount'}>
                <div>Layers</div>
                {incDecInput(layers.length, setLayersAmount, 1, true, 1, 100)}</div>
            <div className={'layers-with-settings'}>
                <LayersSettings modificationService={props.modificationService}
                    updateLayer={updateLayer} layers={layers}/>
                <div id='layers' style={neuronsStyle}/>
            </div>
        </div>
    )
}

export default EditorWindow
