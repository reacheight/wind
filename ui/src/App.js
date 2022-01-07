import React from 'react';

import Analysis from './Analysis/Analysis'
import Form from 'react-bootstrap/Form';

import './App.css';

class App extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      matchId: '',
      analysis: {},
      error: false
    };

    this.handleChange = this.handleChange.bind(this)
    this.handleSubmit = this.handleSubmit.bind(this)
  }

  handleChange(event) {
    this.setState({ matchId: event.target.value })
  }

  handleSubmit(event) {
    this.setState({ error: false })
    fetch(`http://localhost:8080/analysis/${this.state.matchId}`)
      .then(response => {
        if (!response.ok) {
          this.setState({ error: true })
        }
        return response.json()
      })
      .then(json => {
        this.setState({ analysis: json })
      })
      .catch(e => {
        this.setState({ error: true })
      })
    event.preventDefault()
  }

  render() {
    const analysis = this.state.analysis

    return (
      <div className="App">
        <form className="MatchInput" onSubmit={this.handleSubmit}>
          <Form.Control type="text" placeholder="Enter match id" value={this.state.matchId} onChange={this.handleChange} />
        </form>
        <Analysis analysis={analysis} />
        {this.state.error &&
          <div> Error occured :( </div>
        }
      </div>
    );
  }
}

export default App;
